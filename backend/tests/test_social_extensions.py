import unittest

from fastapi import HTTPException
from fastapi.testclient import TestClient
from pydantic import ValidationError

from app import social_extensions
from app.main import app


class SocialLifecycleTests(unittest.TestCase):
    def test_password_change_requires_strong_new_password(self):
        with self.assertRaises(ValidationError):
            social_extensions.PasswordChangeRequest(
                currentPassword="old-password",
                newPassword="short",
            )

    def test_account_deletion_requires_explicit_confirmation(self):
        with self.assertRaises(ValidationError):
            social_extensions.DeleteAccountRequest(confirmation="YES")
        payload = social_extensions.DeleteAccountRequest(confirmation="DELETE")
        self.assertEqual(payload.confirmation, "DELETE")

    def test_uuid_parser_fails_closed(self):
        with self.assertRaises(HTTPException) as ctx:
            social_extensions._parse_uuid("not-a-uuid", "Introuvable")
        self.assertEqual(ctx.exception.status_code, 404)

    def test_extension_routes_are_mounted(self):
        paths = {getattr(route, "path", None) for route in app.routes}
        expected = {
            "/api/social/friends/requests/sent",
            "/api/social/me/password",
            "/api/social/me/friend-code/rotate",
            "/api/social/active-room",
        }
        self.assertTrue(expected.issubset(paths))

    def test_extension_routes_require_authentication(self):
        client = TestClient(app)
        self.assertEqual(client.get("/api/social/friends/requests/sent").status_code, 401)
        self.assertEqual(client.get("/api/social/active-room").status_code, 401)
        self.assertEqual(client.post("/api/social/me/friend-code/rotate").status_code, 401)
        self.assertEqual(
            client.post(
                "/api/social/me/password",
                json={"currentPassword": "old-password", "newPassword": "new-password-123"},
            ).status_code,
            401,
        )


if __name__ == "__main__":
    unittest.main()
