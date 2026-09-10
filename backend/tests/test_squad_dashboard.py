import unittest

from fastapi.testclient import TestClient

from app import squad_dashboard
from app.main import app


class SquadDashboardTests(unittest.TestCase):
    def test_ratio_percent_handles_empty_and_normal_scores(self):
        self.assertEqual(squad_dashboard._ratio_percent(0, 0), 0)
        self.assertEqual(squad_dashboard._ratio_percent(7, 10), 70)
        self.assertEqual(squad_dashboard._ratio_percent(1, 3), 33)

    def test_ratio_percent_clamps_invalid_aggregates(self):
        self.assertEqual(squad_dashboard._ratio_percent(-2, 10), 0)
        self.assertEqual(squad_dashboard._ratio_percent(12, 10), 100)

    def test_dashboard_route_requires_authentication(self):
        client = TestClient(app)
        response = client.get("/api/social/squad/dashboard")
        self.assertEqual(response.status_code, 401)


if __name__ == "__main__":
    unittest.main()
