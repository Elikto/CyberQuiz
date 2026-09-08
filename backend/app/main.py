import json
import logging
import os
import smtplib
import ssl
import threading
import time
from email.message import EmailMessage
from secrets import compare_digest
from typing import Literal

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException, Request
from openai import OpenAI
from pydantic import BaseModel, Field, field_validator

load_dotenv()
logger = logging.getLogger("cyberquiz.backend")
app = FastAPI(title="CyberQuiz AI Backend", version="1.1.0")

CONTACT_RATE_LIMIT = 5
CONTACT_RATE_WINDOW_SECONDS = 60 * 60
_contact_attempts: dict[str, list[float]] = {}
_contact_rate_lock = threading.Lock()

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


def _contact_configuration() -> dict[str, object]:
    if not _contact_enabled():
        raise HTTPException(503, "Le service de contact est temporairement indisponible")

    host = os.getenv("CYBERQUIZ_SMTP_HOST", "").strip()
    to_address = os.getenv("CYBERQUIZ_CONTACT_TO", "").strip()
    from_address = os.getenv("CYBERQUIZ_CONTACT_FROM", "").strip()
    username = os.getenv("CYBERQUIZ_SMTP_USERNAME", "").strip()
    password = os.getenv("CYBERQUIZ_SMTP_PASSWORD", "")
    security = os.getenv("CYBERQUIZ_SMTP_SECURITY", "starttls").strip().lower()

    try:
        port = int(os.getenv("CYBERQUIZ_SMTP_PORT", "587"))
    except ValueError:
        port = 0

    if not host or not to_address or not from_address or port not in range(1, 65536):
        logger.error("Contact service enabled with incomplete SMTP configuration")
        raise HTTPException(503, "Le service de contact est temporairement indisponible")

    if security not in {"starttls", "ssl"}:
        logger.error("Unsupported SMTP security mode for contact service")
        raise HTTPException(503, "Le service de contact est temporairement indisponible")

    if bool(username) != bool(password):
        logger.error("SMTP username/password configuration is incomplete")
        raise HTTPException(503, "Le service de contact est temporairement indisponible")

    return {
        "host": host,
        "port": port,
        "to_address": to_address,
        "from_address": from_address,
        "username": username,
        "password": password,
        "security": security,
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


def _send_contact_email(req: ContactRequest, config: dict[str, object]) -> None:
    email = EmailMessage()
    email["Subject"] = CONTACT_SUBJECTS[req.reason]
    email["From"] = str(config["from_address"])
    email["To"] = str(config["to_address"])
    email.set_content(
        "\n".join(
            [
                f"Motif : {CONTACT_LABELS[req.reason]}",
                f"Version CyberQuiz : {req.appVersion}",
                f"Plateforme : {req.platform}",
                "",
                req.message,
            ]
        )
    )

    tls_context = ssl.create_default_context()
    host = str(config["host"])
    port = int(config["port"])
    username = str(config["username"])
    password = str(config["password"])

    if config["security"] == "ssl":
        with smtplib.SMTP_SSL(host, port, timeout=10, context=tls_context) as server:
            if username:
                server.login(username, password)
            server.send_message(email)
    else:
        with smtplib.SMTP(host, port, timeout=10) as server:
            server.ehlo()
            server.starttls(context=tls_context)
            server.ehlo()
            if username:
                server.login(username, password)
            server.send_message(email)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/contact", response_model=ContactResponse)
def contact(req: ContactRequest, request: Request):
    config = _contact_configuration()
    client_id = request.client.host if request.client else "unknown"
    _check_contact_rate_limit(client_id)

    try:
        _send_contact_email(req, config)
    except Exception:
        logger.exception("Contact message delivery failed")
        raise HTTPException(502, "Le message n'a pas pu être envoyé. Réessaie plus tard.") from None

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
