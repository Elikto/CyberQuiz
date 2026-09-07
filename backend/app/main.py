import json
import logging
import os
from secrets import compare_digest
from typing import Literal

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException
from openai import OpenAI
from pydantic import BaseModel, Field

load_dotenv()
logger = logging.getLogger("cyberquiz.backend")
app = FastAPI(title="CyberQuiz AI Backend", version="1.0.0")


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


@app.get("/health")
def health():
    return {"status": "ok"}


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
