import os
import re
import unittest
from unittest.mock import patch
from uuid import uuid4

from fastapi import HTTPException
from pydantic import ValidationError

from app import social
from app.main import app


class SocialSecurityTests(unittest.TestCase):
    def test_email_is_normalized_and_validated(self):
        self.assertEqual(
            social.normalize_email("  Player@Example.COM "),
            "player@example.com",
        )
        with self.assertRaises(ValueError):
            social.normalize_email("not-an-email")
        with self.assertRaises(ValueError):
            social.normalize_email("player@localhost")

    def test_password_hash_never_contains_plaintext_and_verifies(self):
        password = "CyberQuiz-2026!"
        hashed = social.hash_password(password)
        self.assertNotIn(password, hashed)
        self.assertTrue(social.verify_password(hashed, password))
        self.assertFalse(social.verify_password(hashed, "incorrect-password"))
        self.assertFalse(social.verify_password(None, password))

    def test_friend_codes_are_unambiguous_and_random_shape(self):
        codes = {social.generate_friend_code() for _ in range(30)}
        self.assertGreater(len(codes), 1)
        self.assertTrue(all(re.fullmatch(r"[A-HJ-NP-Z2-9]{8}", code) for code in codes))
        self.assertTrue(
            all(
                "I" not in code and "O" not in code and "0" not in code and "1" not in code
                for code in codes
            )
        )

    def test_room_request_rejects_invalid_friend_ids_and_question_ids(self):
        with self.assertRaises(ValidationError):
            social.RoomCreateRequest(inviteeIds=["not-a-uuid"], questionIds=[1])
        with self.assertRaises(ValidationError):
            social.RoomCreateRequest(inviteeIds=[str(uuid4())], questionIds=[0])

    def test_jwt_round_trip_and_wrong_secret_is_rejected(self):
        user_id = str(uuid4())
        with patch.dict(
            os.environ,
            {
                "DATABASE_URL": "postgresql://unused.example/test",
                "CYBERQUIZ_AUTH_JWT_SECRET": "a" * 64,
            },
            clear=True,
        ):
            token = social._issue_token(user_id)
            self.assertEqual(social.decode_token(token), user_id)

            os.environ["CYBERQUIZ_AUTH_JWT_SECRET"] = "b" * 64
            with self.assertRaises(HTTPException) as ctx:
                social.decode_token(token)
            self.assertEqual(ctx.exception.status_code, 401)

    def test_social_config_fails_closed_when_secrets_missing(self):
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(HTTPException) as ctx:
                social._require_social_config()
        self.assertEqual(ctx.exception.status_code, 503)

    def test_google_config_endpoint_exposes_only_public_client_id(self):
        with patch.dict(
            os.environ,
            {"CYBERQUIZ_GOOGLE_CLIENT_ID": "public-client.apps.googleusercontent.com"},
            clear=True,
        ):
            result = social.social_config()
        self.assertEqual(
            result,
            {
                "googleClientId": "public-client.apps.googleusercontent.com",
                "googleEnabled": True,
            },
        )
        self.assertNotIn("secret", str(result).lower())

    def test_social_router_is_mounted_on_main_app(self):
        paths = {route.path for route in app.routes}
        self.assertIn("/api/social/config", paths)
        self.assertIn("/api/social/auth/register", paths)
        self.assertIn("/api/social/friends", paths)
        self.assertIn("/api/social/quiz-rooms", paths)


if __name__ == "__main__":
    unittest.main()
