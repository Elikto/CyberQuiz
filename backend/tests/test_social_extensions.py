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

    def test_extension_routes_are_mounted_and_require_authentication(self):
        client = TestClient(app)
        probes = [
            ("GET", "/api/social/friends/requests/sent", None),
            ("GET", "/api/social/active-room", None),
            ("POST", "/api/social/me/friend-code/rotate", None),
            (
                "POST",
                "/api/social/me/password",
                {"currentPassword": "old-password", "newPassword": "new-password-123"},
            ),
        ]
        for method, path, payload in probes:
            response = client.request(method, path, json=payload)
            self.assertEqual(response.status_code, 401, msg=f"{method} {path}")


if __name__ == "__main__":
    unittest.main()
