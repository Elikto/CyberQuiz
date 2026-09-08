import base64
import hashlib
import json
import logging
import os
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from secrets import compare_digest
from typing import Literal
from uuid import uuid4

from dotenv import load_dotenv
from fastapi import BackgroundTasks, FastAPI, Header, HTTPException, Request
from pydantic import BaseModel, Field, field_validator

load_dotenv()
logger = logging.getLogger("cyberquiz.backend")
app = FastAPI(title="CyberQuiz AI Backend", version="1.4.0")

CONTACT_RATE_LIMIT = 5
CONTACT_RATE_WINDOW_SECONDS = 60 * 60
CONTACT_MESSAGE_PART_SIZE = 1500
URGENT_SMS_RATE_LIMIT = 10
URGENT_SMS_RATE_WINDOW_SECONDS = 60 * 60
TWILIO_SMS_BODY_MAX_CHARS = 1600
RESEND_EMAILS_URL = "https://api.resend.com/emails"
RESEND_CONTACTS_URL = "https://api.resend.com/contacts"
_contact_attempts: dict[str, list[float]] = {}
_contact_rate_lock = threading.Lock()
_urgent_sms_attempts: list[float] = []
_urgent_sms_rate_lock = threading.Lock()

CONTACT_SUBJECTS: dict[str, str] = {
    "bug": "[CyberQuiz] Signalement de bug",
    "improvement": "[CyberQuiz] Proposition d'amélioration",
    "update": "[CyberQuiz] Problème de mise à jour",
    "content": "[CyberQuiz] Question sur un quiz ou contenu",
    "support": "[CyberQuiz] Demande d'aide",
    "other": "[CyberQuiz] Contact",
}

CONTACT_LABELS: dict[str, str] = {
    "bug": "Signaler un bug",
    "improvement": "Proposer une amélioration",
    "update": "Problème de mise à jour",
    "content": "Question sur un quiz / contenu",
    "support": "Aide / support",
    "other": "Autre",
}


class GenerateRequest(BaseModel):
    category: str = Field(default="Cybersécurité", min_length=1, max_length=80)
    difficulty: Literal["EASY", "MEDIUM", "HARD"] = "MEDIUM"
    count: int = Field(default=1, ge=1, le=5)


class Question(BaseModel):
    category: str
    difficulty: str
    question: str
    answers: list[str] = Field(min_length=4, max_length=4)
    correctIndex: int = Field(ge=0, le=3)
    explanation: str


class ContactRequest(BaseModel):
    reason: Literal["bug", "improvement", "update", "content", "support", "other"]
    message: str = Field(min_length=1, max_length=3000)
    appVersion: str = Field(default="inconnue", max_length=80)
    platform: str = Field(default="Android", max_length=80)
    urgent: bool = False
    submissionId: str | None = Field(default=None, min_length=8, max_length=80)

    @field_validator("message")
    @classmethod
    def message_must_not_be_blank(cls, value: str) -> str:
        cleaned = value.strip()
        if not cleaned:
            raise ValueError("Le message ne peut pas être vide")
        return cleaned


class ContactResponse(BaseModel):
    sent: bool


def _generation_enabled() -> bool:
    return os.getenv("CYBERQUIZ_ENABLE_GENERATION", "false").strip().lower() in {
        "1",
        "true",
        "yes",
        "on",
    }


def _require_generation_access(provided_key: str | None) -> None:
    if not _generation_enabled():
        raise HTTPException(404, "Not found")

    expected_key = os.getenv("CYBERQUIZ_ADMIN_KEY", "")
    if not expected_key:
        logger.error("Question generation enabled without CYBERQUIZ_ADMIN_KEY")
        raise HTTPException(503, "Service de génération indisponible")

    if provided_key is None or not compare_digest(provided_key, expected_key):
        raise HTTPException(403, "Accès refusé")


def _contact_enabled() -> bool:
    return os.getenv("CYBERQUIZ_CONTACT_ENABLED", "false").strip().lower() in {
        "1",
        "true",
        "yes",
        "on",
    }


def _urgent_sms_enabled() -> bool:
    return os.getenv("CYBERQUIZ_URGENT_SMS_ENABLED", "false").strip().lower() in {
        "1",
        "true",
        "yes",
        "on",
    }


def _contact_configuration() -> dict[str, str]:
    if not _contact_enabled():
        raise HTTPException(503, "Le service de contact est temporairement indisponible")

    api_key = os.getenv("RESEND_API_KEY", "").strip()
    inbox_segment_id = os.getenv("CYBERQUIZ_CONTACT_INBOX_SEGMENT_ID", "").strip()
    to_address = os.getenv("CYBERQUIZ_CONTACT_TO", "").strip()
    from_address = os.getenv("CYBERQUIZ_CONTACT_FROM", "").strip()
    sms_account_sid = os.getenv("TWILIO_ACCOUNT_SID", "").strip()
    sms_auth_token = os.getenv("TWILIO_AUTH_TOKEN", "").strip()
    sms_from_number = os.getenv("TWILIO_FROM_NUMBER", "").strip()
    sms_to_number = os.getenv("CYBERQUIZ_URGENT_SMS_TO", "").strip()
    sms_enabled = _urgent_sms_enabled()

    if not api_key or not inbox_segment_id:
        logger.error("Contact service enabled without Resend inbox configuration")
        raise HTTPException(503, "Le service de contact est temporairement indisponible")

    if bool(to_address) != bool(from_address):
        logger.warning("Contact email notification configuration is incomplete; notifications disabled")
        to_address = ""
        from_address = ""

    if sms_enabled and not all(
        [sms_account_sid, sms_auth_token, sms_from_number, sms_to_number]
    ):
        logger.warning("Urgent SMS configuration is incomplete; SMS notifications disabled")
        sms_enabled = False
        sms_account_sid = ""
        sms_auth_token = ""
        sms_from_number = ""
        sms_to_number = ""

    return {
        "api_key": api_key,
        "inbox_segment_id": inbox_segment_id,
        "to_address": to_address,
        "from_address": from_address,
        "sms_enabled": "true" if sms_enabled else "",
        "sms_account_sid": sms_account_sid,
        "sms_auth_token": sms_auth_token,
        "sms_from_number": sms_from_number,
        "sms_to_number": sms_to_number,
    }


def _check_contact_rate_limit(client_id: str, now: float | None = None) -> None:
    timestamp = time.monotonic() if now is None else now
    cutoff = timestamp - CONTACT_RATE_WINDOW_SECONDS

    with _contact_rate_lock:
        recent = [attempt for attempt in _contact_attempts.get(client_id, []) if attempt > cutoff]
        if len(recent) >= CONTACT_RATE_LIMIT:
            _contact_attempts[client_id] = recent
            raise HTTPException(429, "Trop de messages envoyés. Réessaie plus tard.")
        recent.append(timestamp)
        _contact_attempts[client_id] = recent


def _allow_urgent_sms_notification(now: float | None = None) -> bool:
    timestamp = time.monotonic() if now is None else now
    cutoff = timestamp - URGENT_SMS_RATE_WINDOW_SECONDS

    with _urgent_sms_rate_lock:
        recent = [attempt for attempt in _urgent_sms_attempts if attempt > cutoff]
        if len(recent) >= URGENT_SMS_RATE_LIMIT:
            _urgent_sms_attempts[:] = recent
            return False
        recent.append(timestamp)
        _urgent_sms_attempts[:] = recent
        return True


def _resend_post(url: str, payload: dict[str, object], api_key: str) -> dict[str, object]:
    request = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        method="POST",
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": "CyberQuiz-Backend/1.4",
        },
    )
    with urllib.request.urlopen(request, timeout=8) as response:
        if response.status < 200 or response.status >= 300:
            raise RuntimeError(f"Resend returned HTTP {response.status}")
        raw = response.read()
        if not raw:
            return {}
        return json.loads(raw.decode("utf-8"))


def _twilio_post(
    account_sid: str,
    auth_token: str,
    form: dict[str, str],
) -> dict[str, object]:
    url = f"https://api.twilio.com/2010-04-01/Accounts/{account_sid}/Messages.json"
    credentials = base64.b64encode(f"{account_sid}:{auth_token}".encode("utf-8")).decode("ascii")
    request = urllib.request.Request(
        url,
        data=urllib.parse.urlencode(form).encode("utf-8"),
        method="POST",
        headers={
            "Authorization": f"Basic {credentials}",
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "application/json",
            "User-Agent": "CyberQuiz-Backend/1.4",
        },
    )
    with urllib.request.urlopen(request, timeout=8) as response:
        if response.status < 200 or response.status >= 300:
            raise RuntimeError(f"Twilio returned HTTP {response.status}")
        raw = response.read()
        if not raw:
            return {}
        return json.loads(raw.decode("utf-8"))


def _contact_subject(req: ContactRequest) -> str:
    subject = CONTACT_SUBJECTS[req.reason]
    return f"[URGENT] {subject}" if req.urgent else subject


def _urgent_sms_text(req: ContactRequest) -> str:
    prefix = "\n".join(
        [
            "URGENT CyberQuiz",
            f"Objet : {_contact_subject(req)}",
            f"Motif : {CONTACT_LABELS[req.reason]}",
            f"Version : {req.appVersion}",
            f"Plateforme : {req.platform}",
            "",
        ]
    )
    suffix = "... [message tronque]"
    available = TWILIO_SMS_BODY_MAX_CHARS - len(prefix)
    message = req.message
    if len(message) > available:
        keep = max(0, available - len(suffix))
        message = message[:keep] + suffix
    return (prefix + message)[:TWILIO_SMS_BODY_MAX_CHARS]


def _submission_token(req: ContactRequest) -> str:
    if not req.submissionId:
        return uuid4().hex
    return hashlib.sha256(req.submissionId.encode("utf-8")).hexdigest()[:32]


def _store_contact_submission(req: ContactRequest, config: dict[str, str]) -> str:
    submission_token = _submission_token(req)
    message_part_1 = req.message[:CONTACT_MESSAGE_PART_SIZE]
    message_part_2 = req.message[CONTACT_MESSAGE_PART_SIZE:]
    payload: dict[str, object] = {
        "email": f"cyberquiz-contact-{submission_token}@example.com",
        "unsubscribed": True,
        "properties": {
            "cq_reason": CONTACT_LABELS[req.reason],
            "cq_message_1": message_part_1,
            "cq_message_2": message_part_2,
            "cq_app_version": req.appVersion,
            "cq_platform": req.platform,
            "cq_urgent": "Oui" if req.urgent else "Non",
            "cq_submitted_at": datetime.now(timezone.utc).isoformat(),
        },
        "segments": [{"id": config["inbox_segment_id"]}],
    }
    try:
        result = _resend_post(RESEND_CONTACTS_URL, payload, config["api_key"])
    except urllib.error.HTTPError as exc:
        # A retry can arrive after the first request was stored but its HTTP response was lost.
        # The deterministic contact email makes a 409 equivalent to successful persistence.
        if exc.code == 409 and req.submissionId:
            return f"existing:{submission_token}"
        raise

    contact_id = str(result.get("id", "")).strip()
    if not contact_id:
        raise RuntimeError("Resend did not return a contact id")
    return contact_id


def _send_contact_email(req: ContactRequest, config: dict[str, str]) -> None:
    if not config.get("from_address") or not config.get("to_address"):
        return

    text = "\n".join(
        [
            f"Motif : {CONTACT_LABELS[req.reason]}",
            f"Version CyberQuiz : {req.appVersion}",
            f"Plateforme : {req.platform}",
            f"Urgent : {'Oui' if req.urgent else 'Non'}",
            "",
            req.message,
        ]
    )
    _resend_post(
        RESEND_EMAILS_URL,
        {
            "from": config["from_address"],
            "to": [config["to_address"]],
            "subject": _contact_subject(req),
            "text": text,
        },
        config["api_key"],
    )


def _send_contact_sms(req: ContactRequest, config: dict[str, str]) -> None:
    if not req.urgent or not config.get("sms_enabled"):
        return

    _twilio_post(
        config["sms_account_sid"],
        config["sms_auth_token"],
        {
            "To": config["sms_to_number"],
            "From": config["sms_from_number"],
            "Body": _urgent_sms_text(req),
        },
    )


def _send_contact_sms_best_effort(req: ContactRequest, config: dict[str, str]) -> None:
    try:
        _send_contact_sms(req, config)
    except Exception:
        logger.warning("Urgent SMS notification failed after message persistence", exc_info=True)


def _send_contact_email_best_effort(req: ContactRequest, config: dict[str, str]) -> None:
    try:
        _send_contact_email(req, config)
    except Exception:
        # Persistence is the source of truth; notification must never delay/fail the request.
        logger.warning("Contact email notification failed after message persistence", exc_info=True)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/contact", response_model=ContactResponse)
def contact(
    req: ContactRequest,
    request: Request,
    background_tasks: BackgroundTasks,
):
    config = _contact_configuration()
    client_id = request.client.host if request.client else "unknown"
    _check_contact_rate_limit(client_id)

    try:
        _store_contact_submission(req, config)
    except Exception:
        logger.exception("Contact message persistence failed")
        raise HTTPException(502, "Le message n'a pas pu être enregistré. Réessaie plus tard.") from None

    if config.get("from_address") and config.get("to_address"):
        background_tasks.add_task(_send_contact_email_best_effort, req, config)

    if req.urgent and config.get("sms_enabled"):
        if _allow_urgent_sms_notification():
            background_tasks.add_task(_send_contact_sms_best_effort, req, config)
        else:
            logger.warning("Urgent SMS notification skipped because the hourly safety cap was reached")

    return ContactResponse(sent=True)


@app.post("/api/questions", response_model=list[Question])
def generate(
    req: GenerateRequest,
    admin_key: str | None = Header(default=None, alias="X-CyberQuiz-Admin-Key"),
):
    _require_generation_access(admin_key)

    key = os.getenv("OPENAI_API_KEY")
    if not key:
        logger.error("Question generation requested without server API credentials")
        raise HTTPException(503, "Service de génération indisponible")

    # Keep the contact API cold start light: OpenAI is imported only for generation requests.
    from openai import OpenAI

    client = OpenAI(api_key=key, timeout=30.0)
    prompt = f"""Génère {req.count} question(s) de quiz de cybersécurité en français.\nCatégorie: {req.category}\nDifficulté: {req.difficulty}\nChaque question doit avoir exactement 4 réponses et une seule correcte.\nRetourne uniquement un JSON valide sous la forme {{\"questions\":[{{\"category\":...,\"difficulty\":...,\"question\":...,\"answers\":[...4...],\"correctIndex\":0-3,\"explanation\":...}}]}}.\nLes questions doivent être techniquement exactes et pédagogiques."""

    try:
        response = client.responses.create(
            model=os.getenv("OPENAI_MODEL", "gpt-5-mini"),
            input=prompt,
            text={"format": {"type": "json_object"}},
        )
        data = json.loads(response.output_text)
        questions = [Question.model_validate(x) for x in data.get("questions", [])]
        if len(questions) != req.count:
            raise ValueError("Nombre de questions invalide")
        return questions
    except Exception:
        logger.exception("Question generation failed")
        raise HTTPException(502, "Génération temporairement indisponible") from None
