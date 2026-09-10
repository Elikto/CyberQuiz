import unittest

from fastapi.testclient import TestClient
from pydantic import ValidationError

from app import question_reports
from app.main import app


class QuestionReportTests(unittest.TestCase):
    def test_payload_accepts_supported_reasons_and_trims_text(self):
        payload = question_reports.QuestionReportRequest(
            questionId=42,
            question="  Quel protocole chiffre le Web ?  ",
            category="  Web  ",
            reason="ambiguous",
            comment="  Deux réponses semblent possibles.  ",
        )
        self.assertEqual(payload.question, "Quel protocole chiffre le Web ?")
        self.assertEqual(payload.category, "Web")
        self.assertEqual(payload.comment, "Deux réponses semblent possibles.")

    def test_payload_rejects_unknown_reason(self):
        with self.assertRaises(ValidationError):
            question_reports.QuestionReportRequest(
                questionId=1,
                question="Question",
                category="Réseau",
                reason="spam",
            )

    def test_payload_rejects_invalid_question_id(self):
        with self.assertRaises(ValidationError):
            question_reports.QuestionReportRequest(
                questionId=0,
                question="Question",
                category="Réseau",
                reason="incorrect",
            )

    def test_report_routes_require_authentication(self):
        client = TestClient(app)
        payload = {
            "questionId": 12,
            "question": "Question de test",
            "category": "Réseau",
            "reason": "incorrect",
            "comment": "La réponse semble fausse.",
        }
        self.assertEqual(client.post("/api/social/question-reports", json=payload).status_code, 401)
        self.assertEqual(client.get("/api/social/question-reports/mine").status_code, 401)


if __name__ == "__main__":
    unittest.main()
