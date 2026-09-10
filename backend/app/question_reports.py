import threading
from datetime import datetime
from typing import Literal

from fastapi import APIRouter, Depends
from pydantic import BaseModel, Field, field_validator

from .social import _connect, _current_user_id, _ensure_schema

# Included under social.router, whose prefix is already /api/social.
router = APIRouter(prefix="/question-reports", tags=["question-reports"])

_SCHEMA_LOCK = threading.Lock()
_SCHEMA_READY = False
_MAX_QUESTION_ID = 2_147_483_647


class QuestionReportRequest(BaseModel):
    questionId: int = Field(ge=1, le=_MAX_QUESTION_ID)
    question: str = Field(min_length=1, max_length=1500)
    category: str = Field(min_length=1, max_length=120)
    reason: Literal["incorrect", "ambiguous", "outdated", "other"]
    comment: str = Field(default="", max_length=800)

    @field_validator("question", "category", "comment")
    @classmethod
    def trim_text(cls, value: str) -> str:
        return value.strip()


class QuestionReportResponse(BaseModel):
    questionId: int
    reason: str
    updatedAt: str | None


def _ensure_question_report_schema() -> None:
    global _SCHEMA_READY
    if _SCHEMA_READY:
        return
    with _SCHEMA_LOCK:
        if _SCHEMA_READY:
            return
        _ensure_schema()
        with _connect() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    CREATE TABLE IF NOT EXISTS cq_question_reports (
                        user_id UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        question_id BIGINT NOT NULL CHECK (question_id > 0),
                        question_text TEXT NOT NULL,
                        category TEXT NOT NULL,
                        reason TEXT NOT NULL CHECK (reason IN ('incorrect', 'ambiguous', 'outdated', 'other')),
                        comment TEXT NOT NULL DEFAULT '',
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        PRIMARY KEY(user_id, question_id)
                    )
                    """
                )
                cur.execute(
                    """
                    CREATE INDEX IF NOT EXISTS cq_question_reports_updated_idx
                    ON cq_question_reports(updated_at DESC)
                    """
                )
            conn.commit()
        _SCHEMA_READY = True


def _response(row) -> dict[str, object]:
    updated_at = row.get("updated_at") if row is not None else None
    return {
        "questionId": int(row["question_id"]),
        "reason": str(row["reason"]),
        "updatedAt": updated_at.isoformat() if isinstance(updated_at, datetime) else None,
    }


@router.post("", response_model=QuestionReportResponse)
def report_question(
    payload: QuestionReportRequest,
    user_id: str = Depends(_current_user_id),
) -> dict[str, object]:
    _ensure_question_report_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                INSERT INTO cq_question_reports(
                    user_id, question_id, question_text, category, reason, comment,
                    created_at, updated_at
                )
                VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (user_id, question_id) DO UPDATE
                SET question_text = EXCLUDED.question_text,
                    category = EXCLUDED.category,
                    reason = EXCLUDED.reason,
                    comment = EXCLUDED.comment,
                    updated_at = NOW()
                RETURNING question_id, reason, updated_at
                """,
                (
                    user_id,
                    payload.questionId,
                    payload.question,
                    payload.category,
                    payload.reason,
                    payload.comment,
                ),
            )
            row = cur.fetchone()
        conn.commit()
    return _response(row)


@router.get("/mine", response_model=list[QuestionReportResponse])
def my_question_reports(
    user_id: str = Depends(_current_user_id),
) -> list[dict[str, object]]:
    _ensure_question_report_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT question_id, reason, updated_at
                FROM cq_question_reports
                WHERE user_id = %s
                ORDER BY updated_at DESC
                LIMIT 500
                """,
                (user_id,),
            )
            rows = cur.fetchall()
    return [_response(row) for row in rows]
