from typing import Any, Literal
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field

from .social import (
    _canonical_friendship,
    _connect,
    _current_user_id,
    _ensure_schema,
    _fetch_user,
    _new_unique_friend_code,
    _is_async_mode,
    _public_user,
    _room_payload,
    hash_password,
    verify_password,
)

router = APIRouter()


class PasswordChangeRequest(BaseModel):
    currentPassword: str = Field(min_length=1, max_length=128)
    newPassword: str = Field(min_length=10, max_length=128)


class DeleteAccountRequest(BaseModel):
    confirmation: Literal["DELETE"]
    currentPassword: str | None = Field(default=None, max_length=128)


def _parse_uuid(value: str, detail: str) -> str:
    try:
        return str(UUID(value))
    except (TypeError, ValueError) as exc:
        raise HTTPException(status_code=404, detail=detail) from exc


@router.get("/friends/requests/sent")
def list_sent_friend_requests(
    user_id: str = Depends(_current_user_id),
) -> list[dict[str, Any]]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT r.id AS request_id, r.created_at,
                       u.id, u.nickname, u.avatar_key, u.level
                FROM cq_friend_requests r
                JOIN cq_users u ON u.id = r.to_user_id
                WHERE r.from_user_id = %s
                ORDER BY r.created_at DESC
                """,
                (user_id,),
            )
            rows = cur.fetchall()
    return [
        {
            "id": str(row["request_id"]),
            "to": _public_user(row),
            "createdAt": row["created_at"].isoformat(),
        }
        for row in rows
    ]


@router.post("/friends/requests/{request_id}/reject")
def reject_friend_request(
    request_id: str,
    user_id: str = Depends(_current_user_id),
) -> dict[str, str]:
    _ensure_schema()
    request_uuid = _parse_uuid(request_id, "Invitation introuvable")
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "DELETE FROM cq_friend_requests WHERE id = %s AND to_user_id = %s RETURNING id",
                (request_uuid, user_id),
            )
            if cur.fetchone() is None:
                raise HTTPException(status_code=404, detail="Invitation introuvable")
        conn.commit()
    return {"status": "rejected"}


@router.delete("/friends/requests/{request_id}")
def cancel_sent_friend_request(
    request_id: str,
    user_id: str = Depends(_current_user_id),
) -> dict[str, str]:
    _ensure_schema()
    request_uuid = _parse_uuid(request_id, "Demande introuvable")
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "DELETE FROM cq_friend_requests WHERE id = %s AND from_user_id = %s RETURNING id",
                (request_uuid, user_id),
            )
            if cur.fetchone() is None:
                raise HTTPException(status_code=404, detail="Demande introuvable")
        conn.commit()
    return {"status": "cancelled"}


@router.delete("/friends/{friend_id}")
def remove_friend(
    friend_id: str,
    user_id: str = Depends(_current_user_id),
) -> dict[str, str]:
    _ensure_schema()
    friend_uuid = _parse_uuid(friend_id, "Ami introuvable")
    if friend_uuid == user_id:
        raise HTTPException(status_code=400, detail="Relation d'ami invalide")
    user_a, user_b = _canonical_friendship(user_id, friend_uuid)
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "DELETE FROM cq_friendships WHERE user_a = %s AND user_b = %s RETURNING user_a",
                (user_a, user_b),
            )
            removed = cur.fetchone()
            cur.execute(
                """
                DELETE FROM cq_friend_requests
                WHERE (from_user_id = %s AND to_user_id = %s)
                   OR (from_user_id = %s AND to_user_id = %s)
                """,
                (user_id, friend_uuid, friend_uuid, user_id),
            )
            if removed is None:
                raise HTTPException(status_code=404, detail="Ami introuvable")
        conn.commit()
    return {"status": "removed"}


@router.post("/me/friend-code/rotate")
def rotate_friend_code(
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            _fetch_user(cur, user_id)
            friend_code = _new_unique_friend_code(cur)
            cur.execute(
                "UPDATE cq_users SET friend_code = %s, updated_at = NOW() WHERE id = %s RETURNING *",
                (friend_code, user_id),
            )
            user = cur.fetchone()
        conn.commit()
    return _public_user(user, True)


@router.post("/me/password")
def change_password(
    payload: PasswordChangeRequest,
    user_id: str = Depends(_current_user_id),
) -> dict[str, str]:
    _ensure_schema()
    if payload.currentPassword == payload.newPassword:
        raise HTTPException(status_code=400, detail="Choisis un nouveau mot de passe différent")
    with _connect() as conn:
        with conn.cursor() as cur:
            user = _fetch_user(cur, user_id)
            password_hash = user.get("password_hash")
            if not password_hash:
                raise HTTPException(
                    status_code=409,
                    detail="Ce compte utilise Google et n'a pas de mot de passe CyberQuiz",
                )
            if not verify_password(password_hash, payload.currentPassword):
                raise HTTPException(status_code=401, detail="Mot de passe actuel incorrect")
            cur.execute(
                "UPDATE cq_users SET password_hash = %s, updated_at = NOW() WHERE id = %s",
                (hash_password(payload.newPassword), user_id),
            )
        conn.commit()
    return {"status": "changed"}


@router.delete("/me")
def delete_account(
    payload: DeleteAccountRequest,
    user_id: str = Depends(_current_user_id),
) -> dict[str, str]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            user = _fetch_user(cur, user_id)
            password_hash = user.get("password_hash")
            if password_hash:
                if not payload.currentPassword or not verify_password(
                    password_hash, payload.currentPassword
                ):
                    raise HTTPException(status_code=401, detail="Mot de passe actuel incorrect")
            cur.execute("DELETE FROM cq_users WHERE id = %s RETURNING id", (user_id,))
            if cur.fetchone() is None:
                raise HTTPException(status_code=404, detail="Compte introuvable")
        conn.commit()
    return {"status": "deleted"}


@router.post("/quiz-invites/{invite_id}/decline")
def decline_room_invite(
    invite_id: str,
    user_id: str = Depends(_current_user_id),
) -> dict[str, str]:
    _ensure_schema()
    invite_uuid = _parse_uuid(invite_id, "Invitation de partie introuvable")
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                DELETE FROM cq_quiz_room_invites
                WHERE id = %s AND to_user_id = %s AND accepted = FALSE
                RETURNING room_id
                """,
                (invite_uuid, user_id),
            )
            declined = cur.fetchone()
            if declined is None:
                raise HTTPException(status_code=404, detail="Invitation de partie introuvable")
            room_id = str(declined["room_id"])
            cur.execute("SELECT mode FROM cq_quiz_rooms WHERE id = %s", (room_id,))
            room = cur.fetchone() or {}
            if _is_async_mode(room.get("mode")):
                cur.execute("SELECT COUNT(*)::INTEGER AS pending FROM cq_quiz_room_invites WHERE room_id = %s AND accepted = FALSE", (room_id,))
                pending = int((cur.fetchone() or {}).get("pending") or 0)
                cur.execute("SELECT BOOL_AND(finished) AS everybody_finished FROM cq_quiz_room_members WHERE room_id = %s", (room_id,))
                done = cur.fetchone() or {}
                if pending == 0 and bool(done.get("everybody_finished")):
                    cur.execute("UPDATE cq_quiz_rooms SET status = 'finished' WHERE id = %s", (room_id,))
        conn.commit()
    return {"status": "declined"}


@router.get("/active-room")
def get_active_room(
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any] | None:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT r.id
                FROM cq_quiz_room_members m
                JOIN cq_quiz_rooms r ON r.id = m.room_id
                WHERE m.user_id = %s
                  AND m.finished = FALSE
                  AND r.status IN ('lobby', 'countdown', 'active')
                  AND r.expires_at > NOW()
                ORDER BY r.created_at DESC
                LIMIT 1
                """,
                (user_id,),
            )
            row = cur.fetchone()
            if row is None:
                return None
            result = _room_payload(cur, str(row["id"]), user_id)
        conn.commit()
    return result


@router.post("/quiz-rooms/{room_id}/cancel")
def cancel_room(
    room_id: str,
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    _ensure_schema()
    room_uuid = _parse_uuid(room_id, "Partie introuvable")
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM cq_quiz_rooms WHERE id = %s FOR UPDATE", (room_uuid,))
            room = cur.fetchone()
            if room is None:
                raise HTTPException(status_code=404, detail="Partie introuvable")
            if str(room["host_user_id"]) != user_id:
                raise HTTPException(status_code=403, detail="Seul l'hôte peut annuler le salon")
            if room["status"] not in {"lobby", "countdown"}:
                raise HTTPException(status_code=409, detail="La partie ne peut plus être annulée")
            cur.execute("UPDATE cq_quiz_rooms SET status = 'finished' WHERE id = %s", (room_uuid,))
            cur.execute(
                "DELETE FROM cq_quiz_room_invites WHERE room_id = %s AND accepted = FALSE",
                (room_uuid,),
            )
            result = _room_payload(cur, room_uuid, user_id)
        conn.commit()
    return result


@router.post("/quiz-rooms/{room_id}/leave")
def leave_room(
    room_id: str,
    user_id: str = Depends(_current_user_id),
) -> dict[str, str]:
    _ensure_schema()
    room_uuid = _parse_uuid(room_id, "Partie introuvable")
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM cq_quiz_rooms WHERE id = %s FOR UPDATE", (room_uuid,))
            room = cur.fetchone()
            if room is None:
                raise HTTPException(status_code=404, detail="Partie introuvable")
            cur.execute(
                "SELECT finished FROM cq_quiz_room_members WHERE room_id = %s AND user_id = %s",
                (room_uuid, user_id),
            )
            member = cur.fetchone()
            if member is None:
                raise HTTPException(status_code=403, detail="Tu ne participes pas à cette partie")

            is_host = str(room["host_user_id"]) == user_id
            if room["status"] in {"lobby", "countdown"}:
                if is_host:
                    cur.execute("UPDATE cq_quiz_rooms SET status = 'finished' WHERE id = %s", (room_uuid,))
                    cur.execute("DELETE FROM cq_quiz_room_invites WHERE room_id = %s", (room_uuid,))
                else:
                    cur.execute(
                        "DELETE FROM cq_quiz_room_members WHERE room_id = %s AND user_id = %s",
                        (room_uuid, user_id),
                    )
                    cur.execute(
                        "DELETE FROM cq_quiz_room_invites WHERE room_id = %s AND to_user_id = %s",
                        (room_uuid, user_id),
                    )
            elif room["status"] == "active":
                cur.execute(
                    "UPDATE cq_quiz_room_members SET finished = TRUE WHERE room_id = %s AND user_id = %s",
                    (room_uuid, user_id),
                )
                cur.execute(
                    "SELECT BOOL_AND(finished) AS everybody_finished FROM cq_quiz_room_members WHERE room_id = %s",
                    (room_uuid,),
                )
                done = cur.fetchone()
                pending_invites = 0
                if _is_async_mode(room.get("mode")):
                    cur.execute("SELECT COUNT(*)::INTEGER AS pending FROM cq_quiz_room_invites WHERE room_id = %s AND accepted = FALSE", (room_uuid,))
                    pending_invites = int((cur.fetchone() or {}).get("pending") or 0)
                if done and done["everybody_finished"] and pending_invites == 0:
                    cur.execute("UPDATE cq_quiz_rooms SET status = 'finished' WHERE id = %s", (room_uuid,))
            else:
                return {"status": "already-finished"}
        conn.commit()
    return {"status": "left"}
