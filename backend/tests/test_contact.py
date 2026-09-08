import json
import os
import unittest
from unittest.mock import MagicMock, patch

from fastapi import HTTPException

from app.main import (
    CONTACT_RATE_LIMIT,
    ContactRequest,
    RESEND_EMAILS_URL,
    _check_contact_rate_limit,
    _contact_attempts,
    _contact_configuration,
    _contact_rate_lock,
    _send_contact_email,
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

    def test_enabled_contact_requires_complete_resend_configuration(self):
        with patch.dict(
            os.environ,
            {"CYBERQUIZ_CONTACT_ENABLED": "true"},
            clear=True,
        ):
            with self.assertRaises(HTTPException) as ctx:
                _contact_configuration()
        self.assertEqual(ctx.exception.status_code, 503)

    def test_contact_configuration_accepts_resend_settings(self):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "RESEND_API_KEY": "re_test_secret",
            "CYBERQUIZ_CONTACT_FROM": "CyberQuiz <contact@example.com>",
            "CYBERQUIZ_CONTACT_TO": "elikto@proton.me",
        }
        with patch.dict(os.environ, env, clear=True):
            config = _contact_configuration()

        self.assertEqual(config["api_key"], "re_test_secret")
        self.assertEqual(config["from_address"], "CyberQuiz <contact@example.com>")
        self.assertEqual(config["to_address"], "elikto@proton.me")

    def test_blank_contact_message_is_rejected(self):
        with self.assertRaises(ValueError):
            ContactRequest(reason="bug", message="   ")

    def test_rate_limit_blocks_sixth_message_in_window(self):
        for index in range(CONTACT_RATE_LIMIT):
            _check_contact_rate_limit("203.0.113.10", now=float(index))

        with self.assertRaises(HTTPException) as ctx:
            _check_contact_rate_limit("203.0.113.10", now=float(CONTACT_RATE_LIMIT))
        self.assertEqual(ctx.exception.status_code, 429)

    @patch("app.main.urllib.request.urlopen")
    def test_contact_email_uses_server_side_subject_and_resend_https(self, urlopen):
        response = MagicMock()
        response.status = 200
        urlopen.return_value.__enter__.return_value = response
        config = {
            "api_key": "re_test_secret",
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

        urlopen.assert_called_once()
        http_request = urlopen.call_args.args[0]
        self.assertEqual(http_request.full_url, RESEND_EMAILS_URL)
        self.assertEqual(http_request.get_method(), "POST")
        self.assertEqual(http_request.get_header("Authorization"), "Bearer re_test_secret")
        payload = json.loads(http_request.data.decode("utf-8"))
        self.assertEqual(payload["subject"], "[CyberQuiz] Signalement de bug")
        self.assertEqual(payload["to"], ["elikto@proton.me"])
        self.assertIn("Le bouton ne répond plus.", payload["text"])


if __name__ == "__main__":
    unittest.main()
