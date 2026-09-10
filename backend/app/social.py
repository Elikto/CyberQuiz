import os
import re
import secrets
import threading
import time
from datetime import datetime, timedelta, timezone
from typing import Any
from uuid import UUID, uuid4

import jwt
import psycopg
from argon2 import PasswordHasher
from argon2.exceptions import InvalidHashError, VerifyMismatchError
from fastapi import APIRouter, Depends, Header, HTTPException, Query, Request
from google.auth.transport import requests as google_requests
from google.oauth2 import id_token as google_id_token
from psycopg.rows import dict_row
from pydantic import BaseModel, Field, field_validator

router = APIRouter(prefix="/api/social", tags=["social"])

_EMAIL_RE = re.compile(r"^[^@\s]{1,64}@[^@\s]{1,190}$")
_FRIEND_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
_PASSWORD_HASHER = PasswordHasher()
_SCHEMA_LOCK = threading.Lock()
_SCHEMA_READY = False
_JWT_ISSUER = "cyberquiz-api"
_JWT_AUDIENCE = "cyberquiz-app"
_JWT_LIFETIME_DAYS = 30
_ROOM_LIFETIME_HOURS = 2
_ASYNC_ROOM_LIFETIME_HOURS = 72
_ROOM_COUNTDOWN_SECONDS = 5
_AUTH_RATE_LIMIT = 20
_AUTH_RATE_WINDOW_SECONDS = 10 * 60
_auth_attempts: dict[str, list[float]] = {}
_auth_rate_lock = threading.Lock()


class RegisterRequest(BaseModel):
    email: str = Field(min_length=3, max_length=254)
    password: str = Field(min_length=10, max_length=128)
    nickname: str = Field(default="Joueur Cyber", min_length=1, max_length=24)
    avatarKey: str = Field(default="beginner", min_length=1, max_length=64)
    level: int = Field(default=1, ge=1, le=999)

    @field_validator("email")
    @classmethod
    def validate_email(cls, value: str) -> str:
        return normalize_email(value)

    @field_validator("nickname")
    @classmethod
    def validate_nickname(cls, value: str) -> str:
        return clean_nickname(value)


class LoginRequest(BaseModel):
    email: str = Field(min_length=3, max_length=254)
    password: str = Field(min_length=1, max_length=128)

    @field_validator("email")
    @classmethod
    def validate_email(cls, value: str) -> str:
        return normalize_email(value)


class GoogleAuthRequest(BaseModel):
    idToken: str = Field(min_length=20, max_length=8192)
    nickname: str | None = Field(default=None, max_length=24)
    avatarKey: str = Field(default="beginner", min_length=1, max_length=64)
    level: int = Field(default=1, ge=1, le=999)


class ProfileUpdateRequest(BaseModel):
    nickname: str = Field(min_length=1, max_length=24)
    avatarKey: str = Field(min_length=1, max_length=64)
    level: int = Field(ge=1, le=999)

    @field_validator("nickname")
    @classmethod
    def validate_nickname(cls, value: str) -> str:
        return clean_nickname(value)


class FriendRequestCreate(BaseModel):
    friendCode: str = Field(min_length=6, max_length=16)

    @field_validator("friendCode")
    @classmethod
    def normalize_code(cls, value: str) -> str:
        return value.strip().upper()


class RoomCreateRequest(BaseModel):
    inviteeIds: list[str] = Field(min_length=1, max_length=7)
    questionIds: list[int] = Field(min_length=1, max_length=200)
    mode: str = Field(default="RANDOM", min_length=1, max_length=32)
    categories: list[str] = Field(default_factory=list, max_length=64)

    @field_validator("inviteeIds")
    @classmethod
    def validate_invitees(cls, value: list[str]) -> list[str]:
        result: list[str] = []
        for raw in value:
            try:
                normalized = str(UUID(raw))
            except (TypeError, ValueError) as exc:
                raise ValueError("Identifiant d'ami invalide") from exc
            if normalized not in result:
                result.append(normalized)
        if not result:
            raise ValueError("Au moins un ami est requis")
        return result

    @field_validator("questionIds")
    @classmethod
    def validate_questions(cls, value: list[int]) -> list[int]:
        if any(item <= 0 for item in value):
            raise ValueError("Identifiant de question invalide")
        return value

    @field_validator("categories")
    @classmethod
    def clean_categories(cls, value: list[str]) -> list[str]:
        return [item.strip()[:80] for item in value if item.strip()]


class ReadyRequest(BaseModel):
    ready: bool = True


class RoomProgressRequest(BaseModel):
    answered: int = Field(ge=0, le=10000)
    correct: int = Field(ge=0, le=10000)
    finished: bool = False


def normalize_email(value: str) -> str:
    cleaned = value.strip().lower()
    if len(cleaned) > 254 or not _EMAIL_RE.fullmatch(cleaned):
        raise ValueError("Adresse e-mail invalide")
    domain = cleaned.rsplit("@", 1)[1]
    if "." not in domain or domain.startswith(".") or domain.endswith("."):
        raise ValueError("Adresse e-mail invalide")
    return cleaned


def clean_nickname(value: str) -> str:
    cleaned = value.strip()
    if not cleaned:
        raise ValueError("Le pseudo ne peut pas être vide")
    return cleaned


def generate_friend_code(length: int = 8) -> str:
    return "".join(secrets.choice(_FRIEND_CODE_ALPHABET) for _ in range(length))


def hash_password(password: str) -> str:
    return _PASSWORD_HASHER.hash(password)


def verify_password(password_hash: str | None, password: str) -> bool:
    if not password_hash:
        return False
    try:
        return _PASSWORD_HASHER.verify(password_hash, password)
    except (VerifyMismatchError, InvalidHashError):
        return False


def _database_url() -> str:
    return os.getenv("DATABASE_URL", "").strip() or os.getenv("CYBERQUIZ_DATABASE_URL", "").strip()


def _jwt_secret() -> str:
    return os.getenv("CYBERQUIZ_AUTH_JWT_SECRET", "").strip()


def _google_client_id() -> str:
    return os.getenv("CYBERQUIZ_GOOGLE_CLIENT_ID", "").strip()


def _require_social_config() -> tuple[str, str]:
    database_url = _database_url()
    jwt_secret = _jwt_secret()
    if not database_url or len(jwt_secret) < 32:
        raise HTTPException(status_code=503, detail="Service de compte temporairement indisponible")
    return database_url, jwt_secret


def _connect():
    database_url, _ = _require_social_config()
    return psycopg.connect(database_url, row_factory=dict_row, connect_timeout=6)


def _ensure_schema() -> None:
    global _SCHEMA_READY
    if _SCHEMA_READY:
        return
    with _SCHEMA_LOCK:
        if _SCHEMA_READY:
            return
        with _connect() as conn:
            with conn.cursor() as cur:
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS cq_users (
                        id UUID PRIMARY KEY,
                        email TEXT NOT NULL UNIQUE,
                        password_hash TEXT,
                        google_sub TEXT UNIQUE,
                        nickname TEXT NOT NULL,
                        avatar_key TEXT NOT NULL DEFAULT 'beginner',
                        level INTEGER NOT NULL DEFAULT 1 CHECK (level >= 1),
                        friend_code TEXT NOT NULL UNIQUE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                """)
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS cq_friend_requests (
                        id UUID PRIMARY KEY,
                        from_user_id UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        to_user_id UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        UNIQUE (from_user_id, to_user_id),
                        CHECK (from_user_id <> to_user_id)
                    )
                """)
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS cq_friendships (
                        user_a UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        user_b UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        PRIMARY KEY (user_a, user_b),
                        CHECK (user_a <> user_b)
                    )
                """)
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS cq_quiz_rooms (
                        id UUID PRIMARY KEY,
                        host_user_id UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        status TEXT NOT NULL DEFAULT 'lobby',
                        question_ids BIGINT[] NOT NULL,
                        mode TEXT NOT NULL DEFAULT 'RANDOM',
                        categories TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
                        starts_at TIMESTAMPTZ,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        expires_at TIMESTAMPTZ NOT NULL,
                        CHECK (status IN ('lobby', 'countdown', 'active', 'finished'))
                    )
                """)
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS cq_quiz_room_members (
                        room_id UUID NOT NULL REFERENCES cq_quiz_rooms(id) ON DELETE CASCADE,
                        user_id UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        ready BOOLEAN NOT NULL DEFAULT FALSE,
                        answered INTEGER NOT NULL DEFAULT 0,
                        correct INTEGER NOT NULL DEFAULT 0,
                        finished BOOLEAN NOT NULL DEFAULT FALSE,
                        joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        PRIMARY KEY (room_id, user_id)
                    )
                """)
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS cq_quiz_room_invites (
                        id UUID PRIMARY KEY,
                        room_id UUID NOT NULL REFERENCES cq_quiz_rooms(id) ON DELETE CASCADE,
                        from_user_id UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        to_user_id UUID NOT NULL REFERENCES cq_users(id) ON DELETE CASCADE,
                        accepted BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        UNIQUE (room_id, to_user_id)
                    )
                """)
                cur.execute("CREATE INDEX IF NOT EXISTS cq_friend_requests_to_idx ON cq_friend_requests(to_user_id)")
                cur.execute("CREATE INDEX IF NOT EXISTS cq_room_invites_to_idx ON cq_quiz_room_invites(to_user_id, accepted)")
            conn.commit()
        _SCHEMA_READY = True


def _check_auth_rate_limit(request: Request, discriminator: str) -> None:
    forwarded = request.headers.get("x-forwarded-for", "")
    ip = forwarded.split(",", 1)[0].strip() or (request.client.host if request.client else "unknown")
    key = f"{ip}:{discriminator[:80]}"
    now = time.monotonic()
    cutoff = now - _AUTH_RATE_WINDOW_SECONDS
    with _auth_rate_lock:
        attempts = [item for item in _auth_attempts.get(key, []) if item >= cutoff]
        if len(attempts) >= _AUTH_RATE_LIMIT:
            raise HTTPException(status_code=429, detail="Trop de tentatives. Réessaie plus tard.")
        attempts.append(now)
        _auth_attempts[key] = attempts


def _issue_token(user_id: str | UUID) -> str:
    _, secret = _require_social_config()
    now = datetime.now(timezone.utc)
    return jwt.encode({
        "sub": str(user_id),
        "iat": now,
        "exp": now + timedelta(days=_JWT_LIFETIME_DAYS),
        "iss": _JWT_ISSUER,
        "aud": _JWT_AUDIENCE,
    }, secret, algorithm="HS256")


def decode_token(token: str) -> str:
    _, secret = _require_social_config()
    try:
        payload = jwt.decode(token, secret, algorithms=["HS256"], audience=_JWT_AUDIENCE, issuer=_JWT_ISSUER)
        return str(UUID(str(payload["sub"])))
    except (jwt.PyJWTError, KeyError, TypeError, ValueError) as exc:
        raise HTTPException(status_code=401, detail="Session invalide ou expirée") from exc


def _current_user_id(authorization: str | None = Header(default=None)) -> str:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Connexion requise")
    return decode_token(authorization[7:].strip())


def _public_user(row: dict[str, Any], include_private: bool = False) -> dict[str, Any]:
    payload: dict[str, Any] = {
        "id": str(row["id"]),
        "nickname": row["nickname"],
        "avatarKey": row.get("avatar_key") or "beginner",
        "level": int(row.get("level") or 1),
    }
    if include_private:
        payload["friendCode"] = row["friend_code"]
        payload["email"] = row["email"]
    return payload


def _fetch_user(cur, user_id: str) -> dict[str, Any]:
    cur.execute("SELECT * FROM cq_users WHERE id = %s", (user_id,))
    row = cur.fetchone()
    if row is None:
        raise HTTPException(status_code=401, detail="Compte introuvable")
    return row


def _canonical_friendship(a: str, b: str) -> tuple[str, str]:
    return (a, b) if a < b else (b, a)


def _are_friends(cur, a: str, b: str) -> bool:
    user_a, user_b = _canonical_friendship(a, b)
    cur.execute("SELECT 1 FROM cq_friendships WHERE user_a = %s AND user_b = %s", (user_a, user_b))
    return cur.fetchone() is not None


def _is_async_mode(mode: str | None) -> bool:
    return (mode or "").upper() == "ASYNC"


def _new_unique_friend_code(cur) -> str:
    for _ in range(12):
        code = generate_friend_code()
        cur.execute("SELECT 1 FROM cq_users WHERE friend_code = %s", (code,))
        if cur.fetchone() is None:
            return code
    raise HTTPException(status_code=503, detail="Impossible de générer un code ami")


def _room_payload(cur, room_id: str, viewer_id: str) -> dict[str, Any]:
    cur.execute("""
        SELECT r.* FROM cq_quiz_rooms r
        JOIN cq_quiz_room_members m ON m.room_id = r.id
        WHERE r.id = %s AND m.user_id = %s
    """, (room_id, viewer_id))
    room = cur.fetchone()
    if room is None:
        raise HTTPException(status_code=404, detail="Partie introuvable")
    now = datetime.now(timezone.utc)
    if room["expires_at"] <= now and room["status"] != "finished":
        cur.execute("UPDATE cq_quiz_rooms SET status = 'finished' WHERE id = %s", (room_id,))
        room["status"] = "finished"
    elif room["status"] == "countdown" and room["starts_at"] and room["starts_at"] <= now:
        cur.execute("UPDATE cq_quiz_rooms SET status = 'active' WHERE id = %s", (room_id,))
        room["status"] = "active"
    cur.execute("""
        SELECT u.id, u.nickname, u.avatar_key, u.level,
               m.ready, m.answered, m.correct, m.finished
        FROM cq_quiz_room_members m
        JOIN cq_users u ON u.id = m.user_id
        WHERE m.room_id = %s ORDER BY m.joined_at ASC
    """, (room_id,))
    members = []
    for member in cur.fetchall():
        item = _public_user(member)
        item.update({
            "ready": bool(member["ready"]),
            "answered": int(member["answered"]),
            "correct": int(member["correct"]),
            "finished": bool(member["finished"]),
        })
        members.append(item)
    return {
        "id": str(room["id"]),
        "hostUserId": str(room["host_user_id"]),
        "status": room["status"],
        "questionIds": [int(item) for item in (room["question_ids"] or [])],
        "mode": room["mode"],
        "categories": list(room["categories"] or []),
        "startsAt": room["starts_at"].isoformat() if room["starts_at"] else None,
        "serverNow": now.isoformat(),
        "members": members,
    }


@router.get("/config")
def social_config() -> dict[str, Any]:
    return {"googleClientId": _google_client_id() or None, "googleEnabled": bool(_google_client_id())}


@router.post("/auth/register")
def register(payload: RegisterRequest, request: Request) -> dict[str, Any]:
    _check_auth_rate_limit(request, payload.email)
    _ensure_schema()
    password_hash = hash_password(payload.password)
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT 1 FROM cq_users WHERE email = %s", (payload.email,))
            if cur.fetchone() is not None:
                raise HTTPException(status_code=409, detail="Un compte existe déjà pour cette adresse")
            user_id = uuid4()
            friend_code = _new_unique_friend_code(cur)
            cur.execute("""
                INSERT INTO cq_users(id, email, password_hash, nickname, avatar_key, level, friend_code)
                VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING *
            """, (user_id, payload.email, password_hash, payload.nickname, payload.avatarKey, payload.level, friend_code))
            user = cur.fetchone()
        conn.commit()
    return {"token": _issue_token(user_id), "user": _public_user(user, True)}


@router.post("/auth/login")
def login(payload: LoginRequest, request: Request) -> dict[str, Any]:
    _check_auth_rate_limit(request, payload.email)
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM cq_users WHERE email = %s", (payload.email,))
            user = cur.fetchone()
    if user is None or not verify_password(user.get("password_hash"), payload.password):
        raise HTTPException(status_code=401, detail="E-mail ou mot de passe incorrect")
    return {"token": _issue_token(user["id"]), "user": _public_user(user, True)}


@router.post("/auth/google")
def google_login(payload: GoogleAuthRequest, request: Request) -> dict[str, Any]:
    client_id = _google_client_id()
    if not client_id:
        raise HTTPException(status_code=503, detail="Connexion Google pas encore configurée")
    _check_auth_rate_limit(request, "google")
    _ensure_schema()
    try:
        claims = google_id_token.verify_oauth2_token(payload.idToken, google_requests.Request(), client_id)
    except Exception as exc:
        raise HTTPException(status_code=401, detail="Identité Google invalide") from exc
    if claims.get("aud") != client_id or not claims.get("email_verified"):
        raise HTTPException(status_code=401, detail="Identité Google invalide")
    google_sub = str(claims.get("sub") or "").strip()
    email = normalize_email(str(claims.get("email") or ""))
    if not google_sub:
        raise HTTPException(status_code=401, detail="Identité Google invalide")
    suggested_name = clean_nickname(payload.nickname or str(claims.get("name") or "Joueur Cyber"))[:24]
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM cq_users WHERE google_sub = %s", (google_sub,))
            user = cur.fetchone()
            if user is None:
                cur.execute("SELECT * FROM cq_users WHERE email = %s", (email,))
                user = cur.fetchone()
                if user is not None:
                    if user.get("google_sub") and user["google_sub"] != google_sub:
                        raise HTTPException(status_code=409, detail="Ce compte est déjà lié à une autre identité Google")
                    cur.execute("UPDATE cq_users SET google_sub = %s, updated_at = NOW() WHERE id = %s RETURNING *", (google_sub, user["id"]))
                    user = cur.fetchone()
                else:
                    user_id = uuid4()
                    friend_code = _new_unique_friend_code(cur)
                    cur.execute("""
                        INSERT INTO cq_users(id, email, google_sub, nickname, avatar_key, level, friend_code)
                        VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING *
                    """, (user_id, email, google_sub, suggested_name, payload.avatarKey, payload.level, friend_code))
                    user = cur.fetchone()
            conn.commit()
    return {"token": _issue_token(user["id"]), "user": _public_user(user, True)}


@router.get("/me")
def me(user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            user = _fetch_user(cur, user_id)
    return _public_user(user, True)


@router.put("/me/profile")
def update_profile(payload: ProfileUpdateRequest, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                UPDATE cq_users SET nickname = %s, avatar_key = %s, level = %s, updated_at = NOW()
                WHERE id = %s RETURNING *
            """, (payload.nickname, payload.avatarKey, payload.level, user_id))
            user = cur.fetchone()
            if user is None:
                raise HTTPException(status_code=401, detail="Compte introuvable")
        conn.commit()
    return _public_user(user, True)


@router.get("/users/search")
def search_users(q: str = Query(min_length=2, max_length=40), user_id: str = Depends(_current_user_id)) -> list[dict[str, Any]]:
    _ensure_schema()
    cleaned = q.strip()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT id, nickname, avatar_key, level FROM cq_users
                WHERE id <> %s AND (UPPER(friend_code) = UPPER(%s) OR nickname ILIKE %s)
                ORDER BY CASE WHEN UPPER(friend_code) = UPPER(%s) THEN 0 ELSE 1 END, nickname LIMIT 20
            """, (user_id, cleaned, f"%{cleaned}%", cleaned))
            rows = cur.fetchall()
    return [_public_user(row) for row in rows]


@router.post("/friends/requests")
def create_friend_request(payload: FriendRequestCreate, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM cq_users WHERE friend_code = %s", (payload.friendCode,))
            target = cur.fetchone()
            if target is None:
                raise HTTPException(status_code=404, detail="Aucun joueur avec ce code ami")
            target_id = str(target["id"])
            if target_id == user_id:
                raise HTTPException(status_code=400, detail="Tu ne peux pas t'ajouter toi-même")
            if _are_friends(cur, user_id, target_id):
                raise HTTPException(status_code=409, detail="Ce joueur est déjà dans tes amis")
            cur.execute("SELECT id FROM cq_friend_requests WHERE from_user_id = %s AND to_user_id = %s", (target_id, user_id))
            reverse = cur.fetchone()
            if reverse is not None:
                user_a, user_b = _canonical_friendship(user_id, target_id)
                cur.execute("INSERT INTO cq_friendships(user_a, user_b) VALUES (%s, %s) ON CONFLICT DO NOTHING", (user_a, user_b))
                cur.execute("DELETE FROM cq_friend_requests WHERE id = %s", (reverse["id"],))
                conn.commit()
                return {"status": "accepted", "friend": _public_user(target)}
            request_id = uuid4()
            cur.execute("""
                INSERT INTO cq_friend_requests(id, from_user_id, to_user_id)
                VALUES (%s, %s, %s) ON CONFLICT (from_user_id, to_user_id) DO NOTHING RETURNING id
            """, (request_id, user_id, target_id))
            created = cur.fetchone()
        conn.commit()
    return {"status": "pending", "requestId": str(created["id"] if created else request_id), "friend": _public_user(target)}


@router.get("/friends/requests")
def list_friend_requests(user_id: str = Depends(_current_user_id)) -> list[dict[str, Any]]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT r.id AS request_id, r.created_at, u.id, u.nickname, u.avatar_key, u.level
                FROM cq_friend_requests r JOIN cq_users u ON u.id = r.from_user_id
                WHERE r.to_user_id = %s ORDER BY r.created_at DESC
            """, (user_id,))
            rows = cur.fetchall()
    return [{"id": str(row["request_id"]), "from": _public_user(row), "createdAt": row["created_at"].isoformat()} for row in rows]


@router.post("/friends/requests/{request_id}/accept")
def accept_friend_request(request_id: str, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    try:
        request_uuid = UUID(request_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Invitation introuvable") from exc
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT r.from_user_id, u.id, u.nickname, u.avatar_key, u.level
                FROM cq_friend_requests r JOIN cq_users u ON u.id = r.from_user_id
                WHERE r.id = %s AND r.to_user_id = %s
            """, (request_uuid, user_id))
            row = cur.fetchone()
            if row is None:
                raise HTTPException(status_code=404, detail="Invitation introuvable")
            from_id = str(row["from_user_id"])
            user_a, user_b = _canonical_friendship(user_id, from_id)
            cur.execute("INSERT INTO cq_friendships(user_a, user_b) VALUES (%s, %s) ON CONFLICT DO NOTHING", (user_a, user_b))
            cur.execute("DELETE FROM cq_friend_requests WHERE (from_user_id = %s AND to_user_id = %s) OR (from_user_id = %s AND to_user_id = %s)", (from_id, user_id, user_id, from_id))
        conn.commit()
    return {"friend": _public_user(row)}


@router.get("/friends")
def list_friends(user_id: str = Depends(_current_user_id)) -> list[dict[str, Any]]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT u.id, u.nickname, u.avatar_key, u.level FROM cq_friendships f
                JOIN cq_users u ON u.id = CASE WHEN f.user_a = %s THEN f.user_b ELSE f.user_a END
                WHERE f.user_a = %s OR f.user_b = %s ORDER BY u.nickname ASC
            """, (user_id, user_id, user_id))
            rows = cur.fetchall()
    return [_public_user(row) for row in rows]


@router.post("/quiz-rooms")
def create_room(payload: RoomCreateRequest, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            _fetch_user(cur, user_id)
            for invitee_id in payload.inviteeIds:
                if invitee_id == user_id or not _are_friends(cur, user_id, invitee_id):
                    raise HTTPException(status_code=403, detail="Une invitation cible un joueur qui n'est pas ton ami")
            room_id = uuid4()
            room_mode = payload.mode.upper()
            lifetime_hours = _ASYNC_ROOM_LIFETIME_HOURS if _is_async_mode(room_mode) else _ROOM_LIFETIME_HOURS
            expires_at = datetime.now(timezone.utc) + timedelta(hours=lifetime_hours)
            cur.execute("""
                INSERT INTO cq_quiz_rooms(id, host_user_id, question_ids, mode, categories, expires_at)
                VALUES (%s, %s, %s, %s, %s, %s)
            """, (room_id, user_id, payload.questionIds, room_mode, payload.categories, expires_at))
            cur.execute("INSERT INTO cq_quiz_room_members(room_id, user_id, ready) VALUES (%s, %s, TRUE)", (room_id, user_id))
            for invitee_id in payload.inviteeIds:
                cur.execute("""
                    INSERT INTO cq_quiz_room_invites(id, room_id, from_user_id, to_user_id)
                    VALUES (%s, %s, %s, %s)
                """, (uuid4(), room_id, user_id, invitee_id))
            room = _room_payload(cur, str(room_id), user_id)
        conn.commit()
    return room


@router.get("/quiz-invites")
def list_room_invites(user_id: str = Depends(_current_user_id)) -> list[dict[str, Any]]:
    _ensure_schema()
    now = datetime.now(timezone.utc)
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT i.id AS invite_id, i.room_id, i.created_at, r.mode, u.id, u.nickname, u.avatar_key, u.level
                FROM cq_quiz_room_invites i
                JOIN cq_quiz_rooms r ON r.id = i.room_id
                JOIN cq_users u ON u.id = i.from_user_id
                WHERE i.to_user_id = %s AND i.accepted = FALSE AND r.expires_at > %s
                  AND (r.status = 'lobby' OR (r.mode = 'ASYNC' AND r.status = 'active'))
                ORDER BY i.created_at DESC
            """, (user_id, now))
            rows = cur.fetchall()
    return [{"id": str(row["invite_id"]), "roomId": str(row["room_id"]), "from": _public_user(row), "mode": row.get("mode") or "RANDOM", "createdAt": row["created_at"].isoformat()} for row in rows]


@router.post("/quiz-invites/{invite_id}/accept")
def accept_room_invite(invite_id: str, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    try:
        invite_uuid = UUID(invite_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Invitation de partie introuvable") from exc
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                SELECT i.*, r.status, r.expires_at, r.mode FROM cq_quiz_room_invites i
                JOIN cq_quiz_rooms r ON r.id = i.room_id
                WHERE i.id = %s AND i.to_user_id = %s AND i.accepted = FALSE
            """, (invite_uuid, user_id))
            invite = cur.fetchone()
            if invite is None:
                raise HTTPException(status_code=404, detail="Invitation de partie introuvable")
            async_mode = _is_async_mode(invite.get("mode"))
            allowed_status = invite["status"] == "lobby" or (async_mode and invite["status"] == "active")
            if invite["expires_at"] <= datetime.now(timezone.utc) or not allowed_status:
                raise HTTPException(status_code=409, detail="Cette invitation n'est plus disponible")
            cur.execute("INSERT INTO cq_quiz_room_members(room_id, user_id, ready) VALUES (%s, %s, %s) ON CONFLICT DO NOTHING", (invite["room_id"], user_id, async_mode))
            cur.execute("UPDATE cq_quiz_room_invites SET accepted = TRUE WHERE id = %s", (invite_uuid,))
            room = _room_payload(cur, str(invite["room_id"]), user_id)
        conn.commit()
    return room


@router.get("/quiz-rooms/{room_id}")
def get_room(room_id: str, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    try:
        room_uuid = str(UUID(room_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Partie introuvable") from exc
    with _connect() as conn:
        with conn.cursor() as cur:
            room = _room_payload(cur, room_uuid, user_id)
        conn.commit()
    return room


@router.post("/quiz-rooms/{room_id}/ready")
def set_room_ready(room_id: str, payload: ReadyRequest, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    try:
        room_uuid = str(UUID(room_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Partie introuvable") from exc
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT status FROM cq_quiz_rooms WHERE id = %s", (room_uuid,))
            room = cur.fetchone()
            if room is None:
                raise HTTPException(status_code=404, detail="Partie introuvable")
            if room["status"] != "lobby":
                raise HTTPException(status_code=409, detail="La partie a déjà démarré")
            cur.execute("UPDATE cq_quiz_room_members SET ready = %s WHERE room_id = %s AND user_id = %s RETURNING user_id", (payload.ready, room_uuid, user_id))
            if cur.fetchone() is None:
                raise HTTPException(status_code=403, detail="Tu ne participes pas à cette partie")
            result = _room_payload(cur, room_uuid, user_id)
        conn.commit()
    return result


@router.post("/quiz-rooms/{room_id}/start")
def start_room(room_id: str, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    try:
        room_uuid = str(UUID(room_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Partie introuvable") from exc
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT * FROM cq_quiz_rooms WHERE id = %s FOR UPDATE", (room_uuid,))
            room = cur.fetchone()
            if room is None:
                raise HTTPException(status_code=404, detail="Partie introuvable")
            if str(room["host_user_id"]) != user_id:
                raise HTTPException(status_code=403, detail="Seul l'hôte peut lancer la partie")
            if room["status"] != "lobby":
                return _room_payload(cur, room_uuid, user_id)
            if _is_async_mode(room.get("mode")):
                starts_at = datetime.now(timezone.utc)
                cur.execute("UPDATE cq_quiz_rooms SET status = 'active', starts_at = %s WHERE id = %s", (starts_at, room_uuid))
            else:
                cur.execute("SELECT ready FROM cq_quiz_room_members WHERE room_id = %s", (room_uuid,))
                readiness = [bool(row["ready"]) for row in cur.fetchall()]
                if len(readiness) < 2:
                    raise HTTPException(status_code=409, detail="Il faut au moins deux joueurs")
                if not all(readiness):
                    raise HTTPException(status_code=409, detail="Tous les joueurs doivent être prêts")
                starts_at = datetime.now(timezone.utc) + timedelta(seconds=_ROOM_COUNTDOWN_SECONDS)
                cur.execute("UPDATE cq_quiz_rooms SET status = 'countdown', starts_at = %s WHERE id = %s", (starts_at, room_uuid))
            result = _room_payload(cur, room_uuid, user_id)
        conn.commit()
    return result


@router.post("/quiz-rooms/{room_id}/progress")
def update_room_progress(room_id: str, payload: RoomProgressRequest, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    try:
        room_uuid = str(UUID(room_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Partie introuvable") from exc
    if payload.correct > payload.answered:
        raise HTTPException(status_code=400, detail="Progression de partie invalide")
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute("""
                UPDATE cq_quiz_room_members SET answered = %s, correct = %s, finished = %s
                WHERE room_id = %s AND user_id = %s RETURNING user_id
            """, (payload.answered, payload.correct, payload.finished, room_uuid, user_id))
            if cur.fetchone() is None:
                raise HTTPException(status_code=403, detail="Tu ne participes pas à cette partie")
            if payload.finished:
                cur.execute("SELECT mode FROM cq_quiz_rooms WHERE id = %s", (room_uuid,))
                room_row = cur.fetchone() or {}
                cur.execute("SELECT BOOL_AND(finished) AS everybody_finished FROM cq_quiz_room_members WHERE room_id = %s", (room_uuid,))
                done = cur.fetchone()
                pending_invites = 0
                if _is_async_mode(room_row.get("mode")):
                    cur.execute("SELECT COUNT(*)::INTEGER AS pending FROM cq_quiz_room_invites WHERE room_id = %s AND accepted = FALSE", (room_uuid,))
                    pending_invites = int((cur.fetchone() or {}).get("pending") or 0)
                if done and done["everybody_finished"] and pending_invites == 0:
                    cur.execute("UPDATE cq_quiz_rooms SET status = 'finished' WHERE id = %s", (room_uuid,))
            result = _room_payload(cur, room_uuid, user_id)
        conn.commit()
    return result
