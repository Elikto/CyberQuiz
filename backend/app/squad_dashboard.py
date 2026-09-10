from datetime import datetime
from typing import Any
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException

from .social import _are_friends, _connect, _current_user_id, _ensure_schema, _public_user

# Included under social.router, whose prefix is already /api/social.
router = APIRouter(prefix="/squad", tags=["social-squad"])

_RECENT_MATCH_LIMIT = 20


def _ratio_percent(correct: int, answered: int) -> int:
    if answered <= 0:
        return 0
    return max(0, min(100, round(correct * 100 / answered)))


def _head_to_head_outcome(my_correct: int, friend_correct: int) -> str:
    if my_correct > friend_correct:
        return "win"
    if my_correct < friend_correct:
        return "loss"
    return "draw"


@router.get("/dashboard")
def squad_dashboard(
    user_id: str = Depends(_current_user_id),
) -> dict[str, Any]:
    _ensure_schema()
    with _connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT
                    COUNT(*)::INTEGER AS played,
                    COALESCE(SUM(me.correct), 0)::INTEGER AS correct,
                    COALESCE(SUM(me.answered), 0)::INTEGER AS answered,
                    COALESCE(SUM(
                        CASE WHEN me.correct = (
                            SELECT MAX(other.correct)
                            FROM cq_quiz_room_members other
                            WHERE other.room_id = r.id AND other.finished = TRUE
                        ) THEN 1 ELSE 0 END
                    ), 0)::INTEGER AS wins
                FROM cq_quiz_rooms r
                JOIN cq_quiz_room_members me
                  ON me.room_id = r.id AND me.user_id = %s
                WHERE r.status = 'finished' AND me.finished = TRUE
                """,
                (user_id,),
            )
            total = cur.fetchone() or {}

            cur.execute(
                """
                WITH friends AS (
                    SELECT CASE WHEN f.user_a = %s THEN f.user_b ELSE f.user_a END AS friend_id
                    FROM cq_friendships f
                    WHERE f.user_a = %s OR f.user_b = %s
                ),
                shared_finished_matches AS (
                    SELECT
                        friend_member.user_id AS friend_id,
                        r.id AS room_id,
                        friend_member.correct,
                        friend_member.answered,
                        CASE WHEN friend_member.correct = (
                            SELECT MAX(other.correct)
                            FROM cq_quiz_room_members other
                            WHERE other.room_id = r.id AND other.finished = TRUE
                        ) THEN 1 ELSE 0 END AS won
                    FROM cq_quiz_rooms r
                    JOIN cq_quiz_room_members me
                      ON me.room_id = r.id
                     AND me.user_id = %s
                     AND me.finished = TRUE
                    JOIN cq_quiz_room_members friend_member
                      ON friend_member.room_id = r.id
                     AND friend_member.finished = TRUE
                    WHERE r.status = 'finished'
                      AND friend_member.user_id <> %s
                )
                SELECT
                    u.id, u.nickname, u.avatar_key, u.level,
                    COUNT(shared.room_id)::INTEGER AS played,
                    COALESCE(SUM(shared.correct), 0)::INTEGER AS correct,
                    COALESCE(SUM(shared.answered), 0)::INTEGER AS answered,
                    COALESCE(SUM(shared.won), 0)::INTEGER AS wins
                FROM friends f
                JOIN cq_users u ON u.id = f.friend_id
                LEFT JOIN shared_finished_matches shared
                  ON shared.friend_id = f.friend_id
                GROUP BY u.id, u.nickname, u.avatar_key, u.level
                ORDER BY wins DESC, played DESC, correct DESC, u.nickname ASC
                LIMIT 100
                """,
                (user_id, user_id, user_id, user_id, user_id),
            )
            leaderboard_rows = cur.fetchall()

            cur.execute(
                """
                SELECT
                    r.id,
                    r.created_at,
                    COALESCE(array_length(r.question_ids, 1), 0)::INTEGER AS question_count,
                    me.correct::INTEGER AS my_correct,
                    me.answered::INTEGER AS my_answered,
                    (
                        SELECT COUNT(*) + 1
                        FROM cq_quiz_room_members other
                        WHERE other.room_id = r.id
                          AND other.finished = TRUE
                          AND other.correct > me.correct
                    )::INTEGER AS rank,
                    (
                        SELECT COUNT(*)
                        FROM cq_quiz_room_members member_count
                        WHERE member_count.room_id = r.id
                    )::INTEGER AS players
                FROM cq_quiz_rooms r
                JOIN cq_quiz_room_members me
                  ON me.room_id = r.id AND me.user_id = %s
                WHERE r.status = 'finished' AND me.finished = TRUE
                ORDER BY r.created_at DESC
                LIMIT %s
                """,
                (user_id, _RECENT_MATCH_LIMIT),
            )
            recent_rows = cur.fetchall()

    played = int(total.get("played") or 0)
    correct = int(total.get("correct") or 0)
    answered = int(total.get("answered") or 0)
    wins = int(total.get("wins") or 0)

    leaderboard: list[dict[str, Any]] = []
    for row in leaderboard_rows:
        item = _public_user(row)
        friend_correct = int(row.get("correct") or 0)
        friend_answered = int(row.get("answered") or 0)
        leaderboard.append(
            {
                "user": item,
                "played": int(row.get("played") or 0),
                "wins": int(row.get("wins") or 0),
                "accuracy": _ratio_percent(friend_correct, friend_answered),
            }
        )

    recent_matches: list[dict[str, Any]] = []
    for row in recent_rows:
        created_at = row.get("created_at")
        recent_matches.append(
            {
                "roomId": str(row["id"]),
                "playedAt": created_at.isoformat() if isinstance(created_at, datetime) else None,
                "questionCount": int(row.get("question_count") or 0),
                "correct": int(row.get("my_correct") or 0),
                "answered": int(row.get("my_answered") or 0),
                "rank": int(row.get("rank") or 1),
                "players": int(row.get("players") or 1),
            }
        )

    return {
        "played": played,
        "wins": wins,
        "accuracy": _ratio_percent(correct, answered),
        "friendLeaderboard": leaderboard,
        "recentMatches": recent_matches,
    }


@router.get("/h2h/{friend_id}")
def squad_head_to_head(friend_id: str, user_id: str = Depends(_current_user_id)) -> dict[str, Any]:
    _ensure_schema()
    try:
        friend_uuid = str(UUID(friend_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail="Ami introuvable") from exc
    with _connect() as conn:
        with conn.cursor() as cur:
            if not _are_friends(cur, user_id, friend_uuid):
                raise HTTPException(status_code=404, detail="Ami introuvable")
            cur.execute("SELECT id, nickname, avatar_key, level FROM cq_users WHERE id = %s", (friend_uuid,))
            friend = cur.fetchone()
            if friend is None:
                raise HTTPException(status_code=404, detail="Ami introuvable")
            cur.execute(
                """
                SELECT r.id, r.created_at, r.mode,
                       COALESCE(array_length(r.question_ids, 1), 0)::INTEGER AS question_count,
                       me.correct::INTEGER AS my_correct, me.answered::INTEGER AS my_answered,
                       them.correct::INTEGER AS friend_correct, them.answered::INTEGER AS friend_answered
                FROM cq_quiz_rooms r
                JOIN cq_quiz_room_members me ON me.room_id = r.id AND me.user_id = %s AND me.finished = TRUE
                JOIN cq_quiz_room_members them ON them.room_id = r.id AND them.user_id = %s AND them.finished = TRUE
                WHERE r.status = 'finished'
                  AND (SELECT COUNT(*) FROM cq_quiz_room_members all_members WHERE all_members.room_id = r.id) = 2
                ORDER BY r.created_at DESC
                LIMIT 50
                """,
                (user_id, friend_uuid),
            )
            rows = cur.fetchall()
    wins = losses = draws = 0
    my_correct = my_answered = friend_correct = friend_answered = 0
    matches: list[dict[str, Any]] = []
    for row in rows:
        outcome = _head_to_head_outcome(int(row["my_correct"]), int(row["friend_correct"]))
        wins += outcome == "win"
        losses += outcome == "loss"
        draws += outcome == "draw"
        my_correct += int(row["my_correct"] or 0)
        my_answered += int(row["my_answered"] or 0)
        friend_correct += int(row["friend_correct"] or 0)
        friend_answered += int(row["friend_answered"] or 0)
        played_at = row.get("created_at")
        matches.append({
            "roomId": str(row["id"]),
            "playedAt": played_at.isoformat() if isinstance(played_at, datetime) else None,
            "questionCount": int(row.get("question_count") or 0),
            "myCorrect": int(row["my_correct"] or 0),
            "friendCorrect": int(row["friend_correct"] or 0),
            "outcome": outcome,
            "mode": row.get("mode") or "RANDOM",
        })
    return {
        "friend": _public_user(friend), "played": len(rows), "wins": wins, "losses": losses, "draws": draws,
        "myAccuracy": _ratio_percent(my_correct, my_answered),
        "friendAccuracy": _ratio_percent(friend_correct, friend_answered),
        "recentMatches": matches,
    }
