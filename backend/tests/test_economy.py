import unittest

from fastapi.testclient import TestClient
from pydantic import ValidationError

from app import economy
from app.main import app


class EconomyTests(unittest.TestCase):
    def test_login_rewards_match_android_rules(self):
        self.assertEqual(economy._login_reward(1), 20)
        self.assertEqual(economy._login_reward(2), 25)
        self.assertEqual(economy._login_reward(7), 50)
        self.assertEqual(economy._login_reward(30), 50)

    def test_level_rewards_match_android_rules(self):
        expected = {1: 10, 5: 15, 10: 20, 15: 25, 20: 30, 25: 40, 30: 60}
        self.assertEqual({level: economy._level_reward(level) for level in expected}, expected)

    def test_metrics_reject_impossible_accuracy(self):
        with self.assertRaises(ValidationError):
            economy.EconomyMetrics(
                answered=2,
                correct=3,
                xp=0,
                level=1,
                streak=0,
                bestStreak=0,
                quizCount=0,
            )

    def test_achievement_rules_match_thresholds(self):
        metrics = economy.EconomyMetrics(
            answered=100,
            correct=85,
            xp=2600,
            level=10,
            streak=2,
            bestStreak=25,
            quizCount=25,
        )
        unlocked = economy._achievement_ids(metrics)
        self.assertTrue(
            {
                "first_answer",
                "questions_10",
                "questions_100",
                "streak_10",
                "streak_25",
                "level_5",
                "level_10",
                "xp_2500",
                "quiz_25",
                "accuracy_80",
            }.issubset(unlocked)
        )
        self.assertNotIn("questions_500", unlocked)
        self.assertNotIn("level_20", unlocked)
        self.assertNotIn("quiz_100", unlocked)

    def test_seed_filters_unknown_ownership_and_claims(self):
        seed = economy.EconomySeed(
            coins=250,
            claimedMissionIds={"answer_10", "invented"},
            unlockedAchievementIds={"first_answer", "invented"},
            claimedLevels={1, 30},
            purchasedFrameKeys={"shop_neon_orbit", "invented"},
            purchasedAvatarKeys={"shop_bug_bot", "invented"},
            purchasedBannerKeys={"shop_zero_trace", "invented"},
        )
        cleaned = economy._clean_seed(seed)
        self.assertEqual(cleaned["claimed_missions"], ["answer_10"])
        self.assertEqual(cleaned["rewarded_achievements"], ["first_answer"])
        self.assertEqual(cleaned["claimed_levels"], [1, 30])
        self.assertEqual(cleaned["purchased_frames"], ["shop_neon_orbit"])
        self.assertEqual(cleaned["purchased_avatars"], ["shop_bug_bot"])
        self.assertEqual(cleaned["purchased_banners"], ["shop_zero_trace"])

    def test_economy_routes_are_mounted_and_authenticated(self):
        client = TestClient(app)
        payload = {
            "metrics": {
                "answered": 0,
                "correct": 0,
                "xp": 0,
                "level": 1,
                "streak": 0,
                "bestStreak": 0,
                "quizCount": 0,
            }
        }
        self.assertEqual(client.post("/api/social/economy/sync", json=payload).status_code, 401)
        self.assertEqual(
            client.post("/api/social/economy/missions/answer_10/claim", json=payload).status_code,
            401,
        )
        self.assertEqual(
            client.post("/api/social/economy/levels/1/claim", json=payload).status_code,
            401,
        )
        purchase = dict(payload)
        purchase.update({"kind": "avatar", "storageKey": "shop_bug_bot"})
        self.assertEqual(client.post("/api/social/economy/purchase", json=purchase).status_code, 401)


if __name__ == "__main__":
    unittest.main()
