import threading
from datetime import date, datetime, timedelta, timezone
from typing import Any, Literal

from fastapi import APIRouter, Depends, HTTPException
from psycopg.types.json import Jsonb
from pydantic import BaseModel, Field, field_validator

from .social import _connect, _current_user_id, _ensure_schema

router = APIRouter(prefix="/api/social/economy", tags=["social-economy"])

_ECONOMY_SCHEMA_LOCK = threading.Lock()
_ECONOMY_SCHEMA_READY = False
_MAX_SEED_COINS = 1_000_000
_MAX_METRIC = 10_000_000
_MAX_LEVEL = 30

_MISSION_DEFINITIONS = {
    "answer_10": ("answered", 10, 40),
    "correct_7": ("correct", 7, 50),
    "xp_75": ("xp", 75, 60),
}

_ACHIEVEMENT_REWARDS = {
    "first_answer": 40,
    "questions_10": 60,
    "questions_100": 150,
    "questions_500": 320,
    "streak_10": 100,
    "streak_25": 220,
    "level_5": 100,
    "level_10": 200,
    "level_20": 400,
    "xp_2500": 300,
    "quiz_25": 240,
    "quiz_100": 500,
    "accuracy_80": 140,
}

_FRAME_CATALOG = {
    "chrome_packet": (200, 1),
    "firewall_ring": (350, 5),
    "void_matrix": (550, 10),
    "shop_neon_orbit": (10, 1),
    "shop_pixel_gate": (10, 1),
    "shop_solar_trace": (10, 1),
    "shop_ice_loop": (10, 1),
    "shop_violet_wave": (10, 1),
}
_AVATAR_CATALOG = {
    "shop_pixel_buddy": (10, 1),
    "shop_bug_bot": (10, 1),
    "shop_fox_zero": (10, 1),
    "shop_drone_eye": (10, 1),
    "shop_root_mecha": (10, 1),
}
_BANNER_CATALOG = {
    "shop_aurora_grid": (10, 1),
    "shop_packet_rain": (10, 1),
    "shop_black_neon": (10, 1),
    "shop_binary_sunset": (10, 1),
    "shop_zero_trace": (10, 1),
}
_PURCHASE_CATALOGS = {
    "frame": _FRAME_CATALOG,
    "avatar": _AVATAR_CATALOG,
    "banner": _BANNER_CATALOG,
}


class EconomyMetrics(BaseModel):
    answered: int = Field(ge=0, le=_MAX_METRIC)
    correct: int = Field(ge=0, le=_MAX_METRIC)
    xp: int = Field(ge=0, le=_MAX_METRIC)
    level: int = Field(ge=1, le=_MAX_LEVEL)
    streak: int = Field(ge=0, le=_MAX_METRIC)
    bestStreak: int = Field(ge=0, le=_MAX_METRIC)
    quizCount: int = Field(ge=0, le=_MAX_METRIC)

    @field_validator("correct")
    @classmethod
    def correct_is_bounded(cls, value: int, info) -> int:
        answered = info.data.get("answered")
        if isinstance(answered, int) and value > answered:
            raise ValueError("correct ne peut pas dépasser answered")
        return value


class EconomySeed(BaseModel):
    coins: int = Field(ge=0, le=_MAX_SEED_COINS)
    loginDay: str | None = Field(default=None, max_length=10)
    loginStreak: int = Field(default=0, ge=0, le=10_000)
    loginRewardToday: int = Field(default=0, ge=0, le=10_000)
    baselineDay: str | None = Field(default=None, max_length=10)
    baselineAnswered: int = Field(default=0, ge=0, le=_MAX_METRIC)
    baselineCorrect: int = Field(default=0, ge=0, le=_MAX_METRIC)
    baselineXp: int = Field(default=0, ge=0, le=_MAX_METRIC)
    claimedMissionIds: set[str] = Field(default_factory=set, max_length=20)
    unlockedAchievementIds: set[str] = Field(default_factory=set, max_length=100)
    claimedLevels: set[int] = Field(default_factory=set, max_length=_MAX_LEVEL)
    purchasedFrameKeys: set[str] = Field(default_factory=set, max_length=100)
    purchasedAvatarKeys: set[str] = Field(default_factory=set, max_length=100)
    purchasedBannerKeys: set[str] = Field(default_factory=set, max_length=100)

    @field_validator("loginDay", "baselineDay")
    @classmethod
    def valid_day(cls, value: str | None) -> str | None:
        if value is None:
            return None
        try:
            date.fromisoformat(value)
        except ValueError as exc:
            raise ValueError("Date invalide") from exc
        return value


class EconomySyncRequest(BaseModel):
    metrics: EconomyMetrics
    seed: EconomySeed | None = None


class LevelClaimRequest(EconomySyncRequest):
    level: int = Field(ge=1, le=_MAX_LEVEL)


class PurchaseRequest(EconomySyncRequest):
    kind: Literal["frame", "avatar", "banner"]
    storageKey: str = Field(min_length=1, max_length=80)


def _ensure_economy_schema() -> None:
    global _ECONOMY_SCHEMA_READY
    if _ECONOMY_SCHEMA_READY:
        return
    with _ECONOMY_SCHEMA_LOCK:
        if _ECONOMY_SCHEMA_READY:
            return
        _ensure_schema()
        with _connect() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    CREATE TABLE IF NOT EXISTS cq_economy_accounts (
                        user_id UUID PRIMARY KEY REFERENCES cq_users(id) ON DELETE CASCADE,
                        coins BIGINT NOT NULL DEFAULT 0 CHECK (coins >= 0),
                        login_day DATE,
                        login_streak INTEGER NOT NULL DEFAULT 0 CHECK (login_streak >= 0),
                        login_reward INTEGER NOT NULL DEFAULT 0 CHECK (login_reward >= 0),
                        baseline_day DATE,
                        baseline_answered INTEGER NOT NULL DEFAULT 0 CHECK (baseline_answered >= 0),
                        baseline_correct INTEGER NOT NULL DEFAULT 0 CHECK (baseline_correct >= 0),
                        baseline_xp INTEGER NOT NULL DEFAULT 0 CHECK (baseline_xp >= 0),
                        claimed_missions JSONB NOT NULL DEFAULT '[]'::jsonb,
                        rewarded_achievements JSONB NOT NULL DEFAULT '[]'::jsonb,
                        claimed_levels JSONB NOT NULL DEFAULT '[]'::jsonb,
                        purchased_frames JSONB NOT NULL DEFAULT '[]'::jsonb,
                        purchased_avatars JSONB NOT NULL DEFAULT '[]'::jsonb,
                        purchased_banners JSONB NOT NULL DEFAULT '[]'::jsonb,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """
                )
            conn.commit()
        _ECONOMY_SCHEMA_READY = True


def _today() -> date:
    return datetime.now(timezone.utc).date()


def _login_reward(streak: int) -> int:
    return 20 + (min(max(streak, 1), 7) - 1) * 5


def _level_reward(level: int) -> int:
    safe = min(max(level, 1), _MAX_LEVEL)
    if safe <= 4:
        return 10
    if safe <= 9:
        return 15
    if safe <= 14:
        return 20
    if safe <= 19:
        return 25
    if safe <= 24:
        return 30
    if safe <= 29:
        return 40
    return 60


def _achievement_ids(metrics: EconomyMetrics) -> set[str]:
    result: set[str] = set()
    if metrics.answered >= 1:
        result.add("first_answer")
    if metrics.answered >= 10:
        result.add("questions_10")
    if metrics.answered >= 100:
        result.add("questions_100")
    if metrics.answered >= 500:
        result.add("questions_500")
    if metrics.bestStreak >= 10:
        result.add("streak_10")
    if metrics.bestStreak >= 25:
        result.add("streak_25")
    if metrics.level >= 5:
        result.add("level_5")
    if metrics.level >= 10:
        result.add("level_10")
    if metrics.level >= 20:
        result.add("level_20")
    if metrics.xp >= 2500:
        result.add("xp_2500")
    if metrics.quizCount >= 25:
        result.add("quiz_25")
    if metrics.quizCount >= 100:
        result.add("quiz_100")
    if metrics.answered >= 25 and metrics.correct * 100 // metrics.answered >= 80:
        result.add("accuracy_80")
    return result


def _clean_seed(seed: EconomySeed | None) -> dict[str, Any]:
    if seed is None:
        return {
            "coins": 0,
            "login_day": None,
            "login_streak": 0,
            "login_reward": 0,
            "baseline_day": None,
            "baseline_answered": 0,
            "baseline_correct": 0,
            "baseline_xp": 0,
            "claimed_missions": [],
            "rewarded_achievements": [],
            "claimed_levels": [],
            "purchased_frames": [],
            "purchased_avatars": [],
            "purchased_banners": [],
        }
    return {
        "coins": seed.coins,
        "login_day": date.fromisoformat(seed.loginDay) if seed.loginDay else None,
        "login_streak": seed.loginStreak,
        "login_reward": seed.loginRewardToday,
        "baseline_day": date.fromisoformat(seed.baselineDay) if seed.baselineDay else None,
        "baseline_answered": seed.baselineAnswered,
        "baseline_correct": min(seed.baselineCorrect, seed.baselineAnswered),
        "baseline_xp": seed.baselineXp,
        "claimed_missions": sorted(set(seed.claimedMissionIds) & set(_MISSION_DEFINITIONS)),
        "rewarded_achievements": sorted(set(seed.unlockedAchievementIds) & set(_ACHIEVEMENT_REWARDS)),
        "claimed_levels": sorted(level for level in seed.claimedLevels if 1 <= level <= _MAX_LEVEL),
        "purchased_frames": sorted(set(seed.purchasedFrameKeys) & set(_FRAME_CATALOG)),
        "purchased_avatars": sorted(set(seed.purchasedAvatarKeys) & set(_AVATAR_CATALOG)),
        "purchased_banners": sorted(set(seed.purchasedBannerKeys) & set(_BANNER_CATALOG)),
    }


def _insert_if_missing(cur, user_id: str, seed: EconomySeed | None) -> None:
    cleaned = _clean_seed(seed)
    cur.execute(
        """
        INSERT INTO cq_economy_accounts(
            user_id, coins, login_day, login_streak, login_reward,
            baseline_day, baseline_answered, baseline_correct, baseline_xp,
            claimed_missions, rewarded_achievements, claimed_levels,
            purchased_frames, purchased_avatars, purchased_banners
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (user_id) DO NOTHING
        """,
        (
            user_id,
            cleaned["coins"],
            cleaned["login_day"],
            cleaned["login_streak"],
            cleaned["login_reward"],
            cleaned["baseline_day"],
            cleaned["baseline_answered"],
            cleaned["baseline_correct"],
            cleaned["baseline_xp"],
            Jsonb(cleaned["claimed_missions"]),
            Jsonb(cleaned["rewarded_achievements"]),
            Jsonb(cleaned["claimed_levels"]),
            Jsonb(cleaned["purchased_frames"]),
            Jsonb(cleaned["purchased_avatars"]),
            Jsonb(cleaned["purchased_banners"]),
        ),
    )


def _locked_account(cur, user_id: str, seed: EconomySeed | None):
    _insert_if_missing(cur, user_id, seed)
    cur.execute("SELECT * FROM cq_economy_accounts WHERE user_id = %s FOR UPDATE", (user_id,))
    row = cur.fetchone()
    if row is None:
        raise HTTPException(status_code=500, detail="Compte économique indisponible")
    return row


def _sync_locked(cur, user_id: str, payload: EconomySyncRequest):
    row = _locked_account(cur, user_id, payload.seed)
    today = _today()
    coins = int(row["coins"])
    login_day = row["login_day"]
    login_streak = int(row["login_streak"])
    login_reward = int(row["login_reward"])
    baseline_day = row["baseline_day"]
    baseline_answered = int(row["baseline_answered"])
    baseline_correct = int(row["baseline_correct"])
    baseline_xp = int(row["baseline_xp"])
    claimed_missions = set(row["claimed_missions"] or [])
    rewarded = set(row["rewarded_achievements"] or [])

    if login_day != today:
        login_streak = login_streak + 1 if login_day == today - timedelta(days=1) else 1
        login_reward = _login_reward(login_streak)
        coins += login_reward
        login_day = today

    if baseline_day != today:
        baseline_day = today
        baseline_answered = payload.metrics.answered
        baseline_correct = payload.metrics.correct
        baseline_xp = payload.metrics.xp
        claimed_missions = set()

    newly_rewarded = _achievement_ids(payload.metrics) - rewarded
    if newly_rewarded:
        coins += sum(_ACHIEVEMENT_REWARDS[item] for item in newly_rewarded)
        rewarded |= newly_rewarded

    cur.execute(
        """
        UPDATE cq_economy_accounts
        SET coins = %s,
            login_day = %s,
            login_streak = %s,
            login_reward = %s,
            baseline_day = %s,
            baseline_answered = %s,
            baseline_correct = %s,
            baseline_xp = %s,
            claimed_missions = %s,
            rewarded_achievements = %s,
            updated_at = NOW()
        WHERE user_id = %s
        RETURNING *
        """,
        (
            coins,
            login_day,
            login_streak,
            login_reward,
            baseline_day,
            baseline_answered,
            baseline_correct,
            baseline_xp,
            Jsonb(sorted(claimed_missions)),
            Jsonb(sorted(rewarded)),
            user_id,
        ),
    )
    return cur.fetchone()


def _state(row) -> dict[str, Any]:
    return {
        "coins": int(row["coins"]),
        "loginDay": row["login_day"].isoformat() if row["login_day"] else None,
        "loginStreak": int(row["login_streak"]),
        "loginRewardToday": int(row["login_reward"]),
        "baselineDay": row["baseline_day"].isoformat() if row["baseline_day"] else None,
        "baselineAnswered": int(row["baseline_answered"]),
        "baselineCorrect": int(row["baseline_correct"]),
        "baselineXp": int(row["baseline_xp"]),
        "claimedMissionIds": sorted(row["claimed_missions"] or []),
        "unlockedAchievementIds": sorted(row["rewarded_achievements"] or []),
        "claimedLevels": sorted(int(value) for value in (row["claimed_levels"] or [])),
        "purchasedFrameKeys": sorted(row["purchased_frames"] or []),
        "purchasedAvatarKeys": sorted(row["purchased_avatars"] or []),
        "purchasedBannerKeys": sorted(row["purchased_banners"] or []),
        "updatedAt": row["updated_at"].isoformat() if row.get("updated_at") else None,
    }


@router.post("/sync")
def sync_economy(
    payload: EconomySyncRequest,
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    _ensure_economy_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            row = _sync_locked(cur, user_id, payload)
        conn.commit()
    return _state(row)


@router.post("/missions/{mission_id}/claim")
def claim_mission(
    mission_id: str,
    payload: EconomySyncRequest,
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    definition = _MISSION_DEFINITIONS.get(mission_id)
    if definition is None:
        raise HTTPException(status_code=404, detail="Mission inconnue")
    _ensure_economy_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            row = _sync_locked(cur, user_id, payload)
            claimed = set(row["claimed_missions"] or [])
            if mission_id in claimed:
                conn.commit()
                return _state(row)
            metric_name, target, reward = definition
            current_value = getattr(payload.metrics, metric_name)
            baseline_field = {
                "answered": "baseline_answered",
                "correct": "baseline_correct",
                "xp": "baseline_xp",
            }[metric_name]
            baseline_value = int(row[baseline_field])
            if current_value - baseline_value < target:
                raise HTTPException(status_code=409, detail="Mission pas encore terminée")
            claimed.add(mission_id)
            cur.execute(
                """
                UPDATE cq_economy_accounts
                SET coins = coins + %s, claimed_missions = %s, updated_at = NOW()
                WHERE user_id = %s
                RETURNING *
                """,
                (reward, Jsonb(sorted(claimed)), user_id),
            )
            row = cur.fetchone()
        conn.commit()
    return _state(row)


@router.post("/levels/{level}/claim")
def claim_level(
    level: int,
    payload: EconomySyncRequest,
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    if level < 1 or level > _MAX_LEVEL:
        raise HTTPException(status_code=404, detail="Niveau inconnu")
    if payload.metrics.level < level:
        raise HTTPException(status_code=409, detail="Niveau pas encore atteint")
    _ensure_economy_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            row = _sync_locked(cur, user_id, payload)
            claimed = {int(value) for value in (row["claimed_levels"] or [])}
            if level in claimed:
                conn.commit()
                return _state(row)
            claimed.add(level)
            cur.execute(
                """
                UPDATE cq_economy_accounts
                SET coins = coins + %s, claimed_levels = %s, updated_at = NOW()
                WHERE user_id = %s
                RETURNING *
                """,
                (_level_reward(level), Jsonb(sorted(claimed)), user_id),
            )
            row = cur.fetchone()
        conn.commit()
    return _state(row)


@router.post("/purchase")
def purchase_cosmetic(
    payload: PurchaseRequest,
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    catalog = _PURCHASE_CATALOGS[payload.kind]
    item = catalog.get(payload.storageKey)
    if item is None:
        raise HTTPException(status_code=404, detail="Cosmétique inconnu")
    cost, required_level = item
    if payload.metrics.level < required_level:
        raise HTTPException(status_code=409, detail="Niveau insuffisant")

    column = {
        "frame": "purchased_frames",
        "avatar": "purchased_avatars",
        "banner": "purchased_banners",
    }[payload.kind]
    _ensure_economy_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            row = _sync_locked(cur, user_id, payload)
            purchased = set(row[column] or [])
            if payload.storageKey in purchased:
                conn.commit()
                return _state(row)
            if int(row["coins"]) < cost:
                raise HTTPException(status_code=409, detail="CyberCoins insuffisants")
            purchased.add(payload.storageKey)
            cur.execute(
                f"""
                UPDATE cq_economy_accounts
                SET coins = coins - %s, {column} = %s, updated_at = NOW()
                WHERE user_id = %s
                RETURNING *
                """,
                (cost, Jsonb(sorted(purchased)), user_id),
            )
            row = cur.fetchone()
        conn.commit()
    return _state(row)
