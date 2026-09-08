import json
import os
import unittest
from unittest.mock import MagicMock, patch

from fastapi import HTTPException
from fastapi.testclient import TestClient

from app.main import (
    CONTACT_MESSAGE_PART_SIZE,
    CONTACT_RATE_LIMIT,
    RESEND_CONTACTS_URL,
    RESEND_EMAILS_URL,
    ContactRequest,
    _check_contact_rate_limit,
    _contact_attempts,
    _contact_configuration,
    _contact_rate_lock,
    _send_contact_email,
    _store_contact_submission,
    app,
)


class ContactServiceTests(unittest.TestCase):
    def setUp(self):
        with _contact_rate_lock:
            _contact_attempts.clear()

    def test_contact_service_is_disabled_by_default(self):
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(HTTPException) as ctx:
                _contact_configuration()
        self.assertEqual(ctx.exception.status_code, 503)

    def test_enabled_contact_requires_inbox_configuration(self):
        with patch.dict(
            os.environ,
            {"CYBERQUIZ_CONTACT_ENABLED": "true"},
            clear=True,
        ):
            with self.assertRaises(HTTPException) as ctx:
                _contact_configuration()
        self.assertEqual(ctx.exception.status_code, 503)

    def test_contact_configuration_accepts_inbox_and_optional_notification(self):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "RESEND_API_KEY": "re_test_secret",
            "CYBERQUIZ_CONTACT_INBOX_SEGMENT_ID": "seg_test",
            "CYBERQUIZ_CONTACT_FROM": "CyberQuiz <contact@example.com>",
            "CYBERQUIZ_CONTACT_TO": "elikto@proton.me",
        }
        with patch.dict(os.environ, env, clear=True):
            config = _contact_configuration()

        self.assertEqual(config["api_key"], "re_test_secret")
        self.assertEqual(config["inbox_segment_id"], "seg_test")
        self.assertEqual(config["from_address"], "CyberQuiz <contact@example.com>")
        self.assertEqual(config["to_address"], "elikto@proton.me")

    def test_contact_configuration_disables_incomplete_email_notification(self):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "RESEND_API_KEY": "re_test_secret",
            "CYBERQUIZ_CONTACT_INBOX_SEGMENT_ID": "seg_test",
            "CYBERQUIZ_CONTACT_TO": "elikto@proton.me",
        }
        with patch.dict(os.environ, env, clear=True):
            config = _contact_configuration()

        self.assertEqual(config["from_address"], "")
        self.assertEqual(config["to_address"], "")

    def test_blank_contact_message_is_rejected(self):
        with self.assertRaises(ValueError):
            ContactRequest(reason="bug", message="   ")

    def test_rate_limit_blocks_sixth_message_in_window(self):
        for index in range(CONTACT_RATE_LIMIT):
            _check_contact_rate_limit("203.0.113.10", now=float(index))

        with self.assertRaises(HTTPException) as ctx:
            _check_contact_rate_limit("203.0.113.10", now=float(CONTACT_RATE_LIMIT))
        self.assertEqual(ctx.exception.status_code, 429)

    @patch("app.main._resend_post")
    def test_submission_is_persisted_as_resend_contact_in_inbox_segment(self, resend_post):
        resend_post.return_value = {"id": "contact_123"}
        config = {
            "api_key": "re_test_secret",
            "inbox_segment_id": "seg_test",
            "from_address": "",
            "to_address": "",
        }
        message = "x" * 3000
        request = ContactRequest(
            reason="bug",
            message=message,
            appVersion="1.0.50",
            platform="Android 16",
        )

        contact_id = _store_contact_submission(request, config)

        self.assertEqual(contact_id, "contact_123")
        resend_post.assert_called_once()
        url, payload, api_key = resend_post.call_args.args
        self.assertEqual(url, RESEND_CONTACTS_URL)
        self.assertEqual(api_key, "re_test_secret")
        self.assertTrue(payload["unsubscribed"])
        self.assertEqual(payload["segments"], [{"id": "seg_test"}])
        self.assertEqual(payload["properties"]["cq_reason"], "Signaler un bug")
        self.assertEqual(len(payload["properties"]["cq_message_1"]), CONTACT_MESSAGE_PART_SIZE)
        self.assertEqual(len(payload["properties"]["cq_message_2"]), 3000 - CONTACT_MESSAGE_PART_SIZE)
        self.assertEqual(
            payload["properties"]["cq_message_1"] + payload["properties"]["cq_message_2"],
            message,
        )
        self.assertTrue(payload["email"].startswith("cyberquiz-contact-"))
        self.assertTrue(payload["email"].endswith("@example.com"))

    @patch("app.main._resend_post")
    def test_contact_email_uses_server_side_subject(self, resend_post):
        resend_post.return_value = {"id": "email_123"}
        config = {
            "api_key": "re_test_secret",
            "inbox_segment_id": "seg_test",
            "from_address": "CyberQuiz <contact@example.com>",
            "to_address": "elikto@proton.me",
        }
        request = ContactRequest(
            reason="bug",
            message="Le bouton ne répond plus.",
            appVersion="1.0.50",
            platform="Android 16",
        )

        _send_contact_email(request, config)

        resend_post.assert_called_once()
        url, payload, api_key = resend_post.call_args.args
        self.assertEqual(url, RESEND_EMAILS_URL)
        self.assertEqual(api_key, "re_test_secret")
        self.assertEqual(payload["subject"], "[CyberQuiz] Signalement de bug")
        self.assertEqual(payload["to"], ["elikto@proton.me"])
        self.assertIn("Le bouton ne répond plus.", payload["text"])

    @patch("app.main._send_contact_email", side_effect=RuntimeError("notification blocked"))
    @patch("app.main._store_contact_submission", return_value="contact_123")
    def test_contact_endpoint_succeeds_when_persisted_even_if_email_notification_fails(
        self,
        store_submission,
        send_email,
    ):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "RESEND_API_KEY": "re_test_secret",
            "CYBERQUIZ_CONTACT_INBOX_SEGMENT_ID": "seg_test",
            "CYBERQUIZ_CONTACT_FROM": "CyberQuiz <contact@example.com>",
            "CYBERQUIZ_CONTACT_TO": "elikto@proton.me",
        }
        with patch.dict(os.environ, env, clear=True):
            response = TestClient(app).post(
                "/api/contact",
                json={
                    "reason": "other",
                    "message": "Message conservé même sans notification.",
                    "appVersion": "test",
                    "platform": "Android",
                },
            )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"sent": True})
        store_submission.assert_called_once()
        send_email.assert_called_once()


if __name__ == "__main__":
    unittest.main()
