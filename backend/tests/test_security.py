import os
import unittest
from unittest.mock import patch

from fastapi import HTTPException

from app.main import _require_generation_access


class GenerationAccessTests(unittest.TestCase):
    def test_generation_is_disabled_by_default(self):
        with patch.dict(os.environ, {}, clear=True):
            with self.assertRaises(HTTPException) as ctx:
                _require_generation_access(None)
        self.assertEqual(ctx.exception.status_code, 404)

    def test_enabled_generation_requires_server_admin_key(self):
        with patch.dict(
            os.environ,
            {"CYBERQUIZ_ENABLE_GENERATION": "true"},
            clear=True,
        ):
            with self.assertRaises(HTTPException) as ctx:
                _require_generation_access("anything")
        self.assertEqual(ctx.exception.status_code, 503)

    def test_wrong_admin_key_is_rejected(self):
        with patch.dict(
            os.environ,
            {
                "CYBERQUIZ_ENABLE_GENERATION": "true",
                "CYBERQUIZ_ADMIN_KEY": "expected-secret",
            },
            clear=True,
        ):
            with self.assertRaises(HTTPException) as ctx:
                _require_generation_access("wrong-secret")
        self.assertEqual(ctx.exception.status_code, 403)

    def test_correct_admin_key_is_accepted(self):
        with patch.dict(
            os.environ,
            {
                "CYBERQUIZ_ENABLE_GENERATION": "true",
                "CYBERQUIZ_ADMIN_KEY": "expected-secret",
            },
            clear=True,
        ):
            _require_generation_access("expected-secret")


if __name__ == "__main__":
    unittest.main()
