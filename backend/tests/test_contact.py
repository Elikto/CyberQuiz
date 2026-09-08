import json
import os
import unittest
import urllib.error
from unittest.mock import MagicMock, patch

from fastapi import HTTPException
from fastapi.testclient import TestClient

from app.main import (
    CONTACT_MESSAGE_PART_SIZE,
    CONTACT_RATE_LIMIT,
    TWILIO_SMS_BODY_MAX_CHARS,
    URGENT_SMS_RATE_LIMIT,
    RESEND_CONTACTS_URL,
    RESEND_EMAILS_URL,
    ContactRequest,
    _allow_urgent_sms_notification,
    _check_contact_rate_limit,
    _contact_attempts,
    _contact_configuration,
    _contact_rate_lock,
    _send_contact_email,
    _send_contact_sms,
    _urgent_sms_attempts,
    _urgent_sms_rate_lock,
    _urgent_sms_text,
    _store_contact_submission,
    _submission_token,
    app,
)


class ContactServiceTests(unittest.TestCase):
    def setUp(self):
        with _contact_rate_lock:
            _contact_attempts.clear()
        with _urgent_sms_rate_lock:
            _urgent_sms_attempts.clear()

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

    def test_contact_configuration_accepts_optional_urgent_sms(self):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "RESEND_API_KEY": "re_test_secret",
            "CYBERQUIZ_CONTACT_INBOX_SEGMENT_ID": "seg_test",
            "CYBERQUIZ_URGENT_SMS_ENABLED": "true",
            "TWILIO_ACCOUNT_SID": "AC123",
            "TWILIO_AUTH_TOKEN": "secret",
            "TWILIO_FROM_NUMBER": "+33123456789",
            "CYBERQUIZ_URGENT_SMS_TO": "+33612345678",
        }
        with patch.dict(os.environ, env, clear=True):
            config = _contact_configuration()

        self.assertEqual(config["sms_enabled"], "true")
        self.assertEqual(config["sms_account_sid"], "AC123")
        self.assertEqual(config["sms_from_number"], "+33123456789")
        self.assertEqual(config["sms_to_number"], "+33612345678")

    def test_incomplete_urgent_sms_configuration_is_disabled_without_breaking_contact(self):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "RESEND_API_KEY": "re_test_secret",
            "CYBERQUIZ_CONTACT_INBOX_SEGMENT_ID": "seg_test",
            "CYBERQUIZ_URGENT_SMS_ENABLED": "true",
            "TWILIO_ACCOUNT_SID": "AC123",
        }
        with patch.dict(os.environ, env, clear=True):
            config = _contact_configuration()

        self.assertEqual(config["sms_enabled"], "")

    def test_blank_contact_message_is_rejected(self):
        with self.assertRaises(ValueError):
            ContactRequest(reason="bug", message="   ")

    def test_contact_request_is_not_urgent_by_default(self):
        request = ContactRequest(reason="other", message="Test")
        self.assertFalse(request.urgent)

    def test_rate_limit_blocks_sixth_message_in_window(self):
        for index in range(CONTACT_RATE_LIMIT):
            _check_contact_rate_limit("203.0.113.10", now=float(index))

        with self.assertRaises(HTTPException) as ctx:
            _check_contact_rate_limit("203.0.113.10", now=float(CONTACT_RATE_LIMIT))
        self.assertEqual(ctx.exception.status_code, 429)

    def test_urgent_sms_hourly_safety_cap(self):
        for index in range(URGENT_SMS_RATE_LIMIT):
            self.assertTrue(_allow_urgent_sms_notification(now=float(index)))

        self.assertFalse(_allow_urgent_sms_notification(now=float(URGENT_SMS_RATE_LIMIT)))

    def test_submission_token_is_stable_for_client_retry_id(self):
        request = ContactRequest(
            reason="other",
            message="Test",
            submissionId="123e4567-e89b-12d3-a456-426614174000",
        )
        self.assertEqual(_submission_token(request), _submission_token(request))

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
            urgent=True,
            submissionId="123e4567-e89b-12d3-a456-426614174000",
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
        self.assertEqual(payload["properties"]["cq_urgent"], "Oui")
        self.assertEqual(len(payload["properties"]["cq_message_1"]), CONTACT_MESSAGE_PART_SIZE)
        self.assertEqual(len(payload["properties"]["cq_message_2"]), 3000 - CONTACT_MESSAGE_PART_SIZE)
        self.assertEqual(
            payload["properties"]["cq_message_1"] + payload["properties"]["cq_message_2"],
            message,
        )
        expected_token = _submission_token(request)
        self.assertEqual(payload["email"], f"cyberquiz-contact-{expected_token}@example.com")

    @patch("app.main._resend_post")
    def test_retry_conflict_is_treated_as_already_persisted(self, resend_post):
        resend_post.side_effect = urllib.error.HTTPError(
            url=RESEND_CONTACTS_URL,
            code=409,
            msg="Conflict",
            hdrs=None,
            fp=None,
        )
        config = {
            "api_key": "re_test_secret",
            "inbox_segment_id": "seg_test",
            "from_address": "",
            "to_address": "",
        }
        request = ContactRequest(
            reason="support",
            message="Retry après timeout",
            submissionId="123e4567-e89b-12d3-a456-426614174000",
        )

        contact_id = _store_contact_submission(request, config)

        self.assertTrue(contact_id.startswith("existing:"))

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

    @patch("app.main._resend_post")
    def test_urgent_contact_email_is_prefixed_and_marked_urgent(self, resend_post):
        resend_post.return_value = {"id": "email_urgent"}
        config = {
            "api_key": "re_test_secret",
            "inbox_segment_id": "seg_test",
            "from_address": "CyberQuiz <contact@example.com>",
            "to_address": "elikto@proton.me",
        }
        request = ContactRequest(
            reason="support",
            message="Besoin d'aide rapidement.",
            urgent=True,
        )

        _send_contact_email(request, config)

        _, payload, _ = resend_post.call_args.args
        self.assertEqual(payload["subject"], "[URGENT] [CyberQuiz] Demande d'aide")
        self.assertIn("Urgent : Oui", payload["text"])

    @patch("app.main._twilio_post")
    def test_urgent_sms_contains_subject_and_message(self, twilio_post):
        twilio_post.return_value = {"sid": "SM123"}
        config = {
            "sms_enabled": "true",
            "sms_account_sid": "AC123",
            "sms_auth_token": "secret",
            "sms_from_number": "+33123456789",
            "sms_to_number": "+33612345678",
        }
        request = ContactRequest(
            reason="bug",
            message="Le quiz est bloqué.",
            appVersion="1.0.50",
            platform="Android 16",
            urgent=True,
        )

        _send_contact_sms(request, config)

        twilio_post.assert_called_once()
        account_sid, auth_token, form = twilio_post.call_args.args
        self.assertEqual(account_sid, "AC123")
        self.assertEqual(auth_token, "secret")
        self.assertEqual(form["To"], "+33612345678")
        self.assertEqual(form["From"], "+33123456789")
        self.assertIn("[URGENT] [CyberQuiz] Signalement de bug", form["Body"])
        self.assertIn("Le quiz est bloqué.", form["Body"])

    @patch("app.main._twilio_post")
    def test_non_urgent_contact_does_not_send_sms(self, twilio_post):
        config = {
            "sms_enabled": "true",
            "sms_account_sid": "AC123",
            "sms_auth_token": "secret",
            "sms_from_number": "+33123456789",
            "sms_to_number": "+33612345678",
        }
        _send_contact_sms(ContactRequest(reason="other", message="Normal"), config)
        twilio_post.assert_not_called()

    def test_urgent_sms_is_capped_to_twilio_body_limit(self):
        request = ContactRequest(reason="other", message="x" * 3000, urgent=True)
        body = _urgent_sms_text(request)
        self.assertLessEqual(len(body), TWILIO_SMS_BODY_MAX_CHARS)
        self.assertTrue(body.endswith("... [message tronque]"))

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
                    "submissionId": "123e4567-e89b-12d3-a456-426614174000",
                },
            )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"sent": True})
        store_submission.assert_called_once()
        send_email.assert_called_once()

    @patch("app.main._send_contact_sms")
    @patch("app.main._send_contact_email")
    @patch("app.main._store_contact_submission", return_value="contact_urgent")
    def test_urgent_contact_endpoint_sends_email_and_sms_in_background(
        self,
        store_submission,
        send_email,
        send_sms,
    ):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "RESEND_API_KEY": "re_test_secret",
            "CYBERQUIZ_CONTACT_INBOX_SEGMENT_ID": "seg_test",
            "CYBERQUIZ_CONTACT_FROM": "CyberQuiz <contact@example.com>",
            "CYBERQUIZ_CONTACT_TO": "elikto@proton.me",
            "CYBERQUIZ_URGENT_SMS_ENABLED": "true",
            "TWILIO_ACCOUNT_SID": "AC123",
            "TWILIO_AUTH_TOKEN": "secret",
            "TWILIO_FROM_NUMBER": "+33123456789",
            "CYBERQUIZ_URGENT_SMS_TO": "+33612345678",
        }
        with patch.dict(os.environ, env, clear=True):
            response = TestClient(app).post(
                "/api/contact",
                json={
                    "reason": "bug",
                    "message": "Urgence test.",
                    "urgent": True,
                    "submissionId": "123e4567-e89b-12d3-a456-426614174001",
                },
            )

        self.assertEqual(response.status_code, 200)
        store_submission.assert_called_once()
        send_email.assert_called_once()
        send_sms.assert_called_once()


if __name__ == "__main__":
    unittest.main()
