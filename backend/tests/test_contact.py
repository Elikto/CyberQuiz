import os
import unittest
from unittest.mock import MagicMock, patch

from fastapi import HTTPException

from app.main import (
    CONTACT_RATE_LIMIT,
    ContactRequest,
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

    def test_enabled_contact_requires_complete_smtp_configuration(self):
        with patch.dict(
            os.environ,
            {"CYBERQUIZ_CONTACT_ENABLED": "true"},
            clear=True,
        ):
            with self.assertRaises(HTTPException) as ctx:
                _contact_configuration()
        self.assertEqual(ctx.exception.status_code, 503)

    def test_contact_configuration_accepts_tls_server_settings(self):
        env = {
            "CYBERQUIZ_CONTACT_ENABLED": "true",
            "CYBERQUIZ_SMTP_HOST": "smtp.example.com",
            "CYBERQUIZ_SMTP_PORT": "587",
            "CYBERQUIZ_SMTP_USERNAME": "user",
            "CYBERQUIZ_SMTP_PASSWORD": "secret",
            "CYBERQUIZ_SMTP_SECURITY": "starttls",
            "CYBERQUIZ_CONTACT_FROM": "sender@example.com",
            "CYBERQUIZ_CONTACT_TO": "elikto@proton.me",
        }
        with patch.dict(os.environ, env, clear=True):
            config = _contact_configuration()

        self.assertEqual(config["host"], "smtp.example.com")
        self.assertEqual(config["port"], 587)
        self.assertEqual(config["to_address"], "elikto@proton.me")
        self.assertEqual(config["security"], "starttls")

    def test_blank_contact_message_is_rejected(self):
        with self.assertRaises(ValueError):
            ContactRequest(reason="bug", message="   ")

    def test_rate_limit_blocks_sixth_message_in_window(self):
        for index in range(CONTACT_RATE_LIMIT):
            _check_contact_rate_limit("203.0.113.10", now=float(index))

        with self.assertRaises(HTTPException) as ctx:
            _check_contact_rate_limit("203.0.113.10", now=float(CONTACT_RATE_LIMIT))
        self.assertEqual(ctx.exception.status_code, 429)

    @patch("app.main.smtplib.SMTP")
    def test_contact_email_uses_server_side_subject_and_tls(self, smtp_cls):
        server = MagicMock()
        smtp_cls.return_value.__enter__.return_value = server
        config = {
            "host": "smtp.example.com",
            "port": 587,
            "username": "user",
            "password": "secret",
            "security": "starttls",
            "from_address": "sender@example.com",
            "to_address": "elikto@proton.me",
        }
        request = ContactRequest(
            reason="bug",
            message="Le bouton ne répond plus.",
            appVersion="1.0.50",
            platform="Android 16",
        )

        _send_contact_email(request, config)

        server.starttls.assert_called_once()
        server.login.assert_called_once_with("user", "secret")
        server.send_message.assert_called_once()
        email = server.send_message.call_args.args[0]
        self.assertEqual(email["Subject"], "[CyberQuiz] Signalement de bug")
        self.assertEqual(email["To"], "elikto@proton.me")
        self.assertIn("Le bouton ne répond plus.", email.get_content())


if __name__ == "__main__":
    unittest.main()
