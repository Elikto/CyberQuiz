import unittest
from uuid import uuid4

from fastapi.testclient import TestClient

from app import social, squad_dashboard
from app.main import app


class CyberSquadAsyncTests(unittest.TestCase):
    def test_async_mode_detection_is_explicit_and_case_insensitive(self):
        self.assertTrue(social._is_async_mode("ASYNC"))
        self.assertTrue(social._is_async_mode("async"))
        self.assertFalse(social._is_async_mode("RANDOM"))
        self.assertFalse(social._is_async_mode(None))

    def test_head_to_head_outcomes(self):
        self.assertEqual(squad_dashboard._head_to_head_outcome(8, 5), "win")
        self.assertEqual(squad_dashboard._head_to_head_outcome(3, 6), "loss")
        self.assertEqual(squad_dashboard._head_to_head_outcome(7, 7), "draw")

    def test_head_to_head_route_requires_authentication(self):
        response = TestClient(app).get(f"/api/social/squad/h2h/{uuid4()}")
        self.assertEqual(response.status_code, 401)


if __name__ == "__main__":
    unittest.main()
