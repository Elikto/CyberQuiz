import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from dotenv import load_dotenv
from openai import OpenAI

load_dotenv()
app = FastAPI(title="CyberQuiz AI Backend", version="1.0.0")

class GenerateRequest(BaseModel):
    category: str = "Cybersécurité"
    difficulty: str = "MEDIUM"
    count: int = Field(default=1, ge=1, le=5)

class Question(BaseModel):
    category: str
    difficulty: str
    question: str
    answers: list[str] = Field(min_length=4, max_length=4)
    correctIndex: int = Field(ge=0, le=3)
    explanation: str

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/api/questions", response_model=list[Question])
def generate(req: GenerateRequest):
    key = os.getenv("OPENAI_API_KEY")
    if not key:
        raise HTTPException(503, "OPENAI_API_KEY n'est pas configurée côté serveur")
    client = OpenAI(api_key=key)
    prompt = f"""Génère {req.count} question(s) de quiz de cybersécurité en français.\nCatégorie: {req.category}\nDifficulté: {req.difficulty}\nChaque question doit avoir exactement 4 réponses et une seule correcte.\nRetourne uniquement un JSON valide sous la forme {{\"questions\":[{{\"category\":...,\"difficulty\":...,\"question\":...,\"answers\":[...4...],\"correctIndex\":0-3,\"explanation\":...}}]}}.\nLes questions doivent être techniquement exactes et pédagogiques."""
    try:
        response = client.responses.create(
            model=os.getenv("OPENAI_MODEL", "gpt-5-mini"),
            input=prompt,
            text={"format": {"type": "json_object"}},
        )
        import json
        data = json.loads(response.output_text)
        questions = [Question.model_validate(x) for x in data.get("questions", [])]
        if len(questions) != req.count:
            raise ValueError("Nombre de questions invalide")
        return questions
    except Exception as exc:
        raise HTTPException(502, f"Génération indisponible: {exc}")
