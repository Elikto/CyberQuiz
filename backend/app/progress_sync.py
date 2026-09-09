import json
import threading
from datetime import datetime
from typing import Any

from fastapi import APIRouter, Depends, HTTPException
from psycopg.types.json import Jsonb
from pydantic import BaseModel, Field, field_validator

from .social import _connect, _current_user_id, _ensure_schema

router = APIRouter(prefix="/api/social", tags=["social"])

_PROGRESS_SCHEMA_LOCK = threading.Lock()
_PROGRESS_SCHEMA_READY = False
_MAX_SNAPSHOT_BYTES = 512 * 1024
_SNAPSHOT_VERSION = 1


class ProgressSnapshotWrite(BaseModel):
    baseRevision: int = Field(ge=0, le=2_147_483_647)
    snapshot: dict[str, Any]

    @field_validator("snapshot")
    @classmethod
    def validate_snapshot(cls, value: dict[str, Any]) -> dict[str, Any]:
        version = value.get("version")
        if version != _SNAPSHOT_VERSION:
            raise ValueError("Version de sauvegarde non prise en charge")
        encoded = json.dumps(value, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
        if len(encoded) > _MAX_SNAPSHOT_BYTES:
            raise ValueError("Sauvegarde trop volumineuse")
        return value


def _ensure_progress_schema() -> None:
    global _PROGRESS_SCHEMA_READY
    if _PROGRESS_SCHEMA_READY:
        return
    with _PROGRESS_SCHEMA_LOCK:
        if _PROGRESS_SCHEMA_READY:
            return
        _ensure_schema()
        with _connect() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    CREATE TABLE IF NOT EXISTS cq_progress_snapshots (
                        user_id UUID PRIMARY KEY REFERENCES cq_users(id) ON DELETE CASCADE,
                        revision BIGINT NOT NULL DEFAULT 0 CHECK (revision >= 0),
                        snapshot JSONB NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """
                )
            conn.commit()
        _PROGRESS_SCHEMA_READY = True


def _envelope(row: dict[str, Any] | None) -> dict[str, Any]:
    if row is None:
        return {"revision": 0, "updatedAt": None, "snapshot": None}
    updated_at = row.get("updated_at")
    return {
        "revision": int(row["revision"]),
        "updatedAt": updated_at.isoformat() if isinstance(updated_at, datetime) else None,
        "snapshot": row["snapshot"],
    }


def _next_revision(current_revision: int, base_revision: int) -> int:
    if current_revision != base_revision:
        raise HTTPException(status_code=409, detail="La progression distante a été modifiée")
    return current_revision + 1


@router.get("/progress")
def get_progress(user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_progress_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT revision, snapshot, updated_at FROM cq_progress_snapshots WHERE user_id = %s",
                (user_id,),
            )
            row = cur.fetchone()
    return _envelope(row)


@router.put("/progress")
def put_progress(
    payload: ProgressSnapshotWrite,
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    _ensure_progress_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT revision FROM cq_progress_snapshots WHERE user_id = %s FOR UPDATE",
                (user_id,),
            )
            current = cur.fetchone()
            current_revision = int(current["revision"]) if current is not None else 0
            next_revision = _next_revision(current_revision, payload.baseRevision)
            if current is None:
                cur.execute(
                    """
                    INSERT INTO cq_progress_snapshots(user_id, revision, snapshot, updated_at)
                    VALUES (%s, %s, %s, NOW())
                    RETURNING revision, snapshot, updated_at
                    """,
                    (user_id, next_revision, Jsonb(payload.snapshot)),
                )
            else:
                cur.execute(
                    """
                    UPDATE cq_progress_snapshots
                    SET revision = %s, snapshot = %s, updated_at = NOW()
                    WHERE user_id = %s
                    RETURNING revision, snapshot, updated_at
                    """,
                    (next_revision, Jsonb(payload.snapshot), user_id),
                )
            row = cur.fetchone()
        conn.commit()
    return _envelope(row)
