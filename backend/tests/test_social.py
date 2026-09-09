import os
import re
from uuid import uuid4

import pytest
from fastapi import HTTPException
from pydantic import ValidationError

from backend.app import social


def test_email_is_normalized_and_validated():
    assert social.normalize_email("  Player@Example.COM ") == "player@example.com"
    with pytest.raises(ValueError):
        social.normalize_email("not-an-email")
    with pytest.raises(ValueError):
        social.normalize_email("player@localhost")


def test_password_hash_never_contains_plaintext_and_verifies():
    password = "CyberQuiz-2026!"
    hashed = social.hash_password(password)
    assert password not in hashed
    assert social.verify_password(hashed, password)
    assert not social.verify_password(hashed, "incorrect-password")
    assert not social.verify_password(None, password)


def test_friend_codes_are_unambiguous_and_random_shape():
    codes = {social.generate_friend_code() for _ in range(30)}
    assert len(codes) > 1
    assert all(re.fullmatch(r"[A-HJ-NP-Z2-9]{8}", code) for code in codes)
    assert all("I" not in code and "O" not in code and "0" not in code and "1" not in code for code in codes)


def test_room_request_rejects_invalid_friend_ids_and_question_ids():
    with pytest.raises(ValidationError):
        social.RoomCreateRequest(inviteeIds=["not-a-uuid"], questionIds=[1])
    with pytest.raises(ValidationError):
        social.RoomCreateRequest(inviteeIds=[str(uuid4())], questionIds=[0])


def test_jwt_round_trip_and_wrong_secret_is_rejected(monkeypatch):
    monkeypatch.setenv("DATABASE_URL", "postgresql://unused.example/test")
    monkeypatch.setenv("CYBERQUIZ_AUTH_JWT_SECRET", "a" * 64)
    user_id = str(uuid4())
    token = social._issue_token(user_id)
    assert social.decode_token(token) == user_id

    monkeypatch.setenv("CYBERQUIZ_AUTH_JWT_SECRET", "b" * 64)
    with pytest.raises(HTTPException) as exc:
        social.decode_token(token)
    assert exc.value.status_code == 401


def test_social_config_fails_closed_when_secrets_missing(monkeypatch):
    monkeypatch.delenv("DATABASE_URL", raising=False)
    monkeypatch.delenv("CYBERQUIZ_DATABASE_URL", raising=False)
    monkeypatch.delenv("CYBERQUIZ_AUTH_JWT_SECRET", raising=False)
    with pytest.raises(HTTPException) as exc:
        social._require_social_config()
    assert exc.value.status_code == 503


def test_google_config_endpoint_exposes_only_public_client_id(monkeypatch):
    monkeypatch.setenv("CYBERQUIZ_GOOGLE_CLIENT_ID", "public-client.apps.googleusercontent.com")
    result = social.social_config()
    assert result == {
        "googleClientId": "public-client.apps.googleusercontent.com",
        "googleEnabled": True,
    }
    assert "secret" not in str(result).lower()
