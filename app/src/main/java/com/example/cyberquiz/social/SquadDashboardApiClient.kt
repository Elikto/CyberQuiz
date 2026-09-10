package com.example.cyberquiz.social

import com.example.cyberquiz.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SquadFriendStats(
    val user: SocialUser,
    val played: Int,
    val wins: Int,
    val accuracy: Int
)

data class SquadRecentMatch(
    val roomId: String,
    val playedAt: String?,
    val questionCount: Int,
    val correct: Int,
    val answered: Int,
    val rank: Int,
    val players: Int
)

data class SquadDashboard(
    val played: Int,
    val wins: Int,
    val accuracy: Int,
    val friendLeaderboard: List<SquadFriendStats>,
    val recentMatches: List<SquadRecentMatch>
)

internal object SquadDashboardApiClient {
    private val endpoint = BuildConfig.SOCIAL_API_URL.trimEnd('/') + "/squad/dashboard"

    init {
        require(URL(endpoint).protocol.equals("https", ignoreCase = true)) {
            "CyberQuiz squad API must use HTTPS"
        }
    }

    suspend fun dashboard(token: String): SquadDashboard = withContext(Dispatchers.IO) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("User-Agent", "CyberQuiz-Android/${BuildConfig.VERSION_NAME}")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching {
                    if (text.isBlank()) null else JSONObject(text).optString("detail").takeIf { it.isNotBlank() }
                }.getOrNull() ?: "Le tableau CyberSquad est indisponible"
                throw SocialApiException(status, message)
            }
            parseDashboard(JSONObject(text))
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseDashboard(json: JSONObject): SquadDashboard {
        val leaderboardJson = json.optJSONArray("friendLeaderboard") ?: JSONArray()
        val recentJson = json.optJSONArray("recentMatches") ?: JSONArray()
        val leaderboard = buildList {
            repeat(leaderboardJson.length()) { index ->
                val item = leaderboardJson.getJSONObject(index)
                add(
                    SquadFriendStats(
                        user = parseUser(item.getJSONObject("user")),
                        played = item.optInt("played", 0).coerceAtLeast(0),
                        wins = item.optInt("wins", 0).coerceAtLeast(0),
                        accuracy = item.optInt("accuracy", 0).coerceIn(0, 100)
                    )
                )
            }
        }
        val recent = buildList {
            repeat(recentJson.length()) { index ->
                val item = recentJson.getJSONObject(index)
                add(
                    SquadRecentMatch(
                        roomId = item.getString("roomId"),
                        playedAt = item.nullableString("playedAt"),
                        questionCount = item.optInt("questionCount", 0).coerceAtLeast(0),
                        correct = item.optInt("correct", 0).coerceAtLeast(0),
                        answered = item.optInt("answered", 0).coerceAtLeast(0),
                        rank = item.optInt("rank", 1).coerceAtLeast(1),
                        players = item.optInt("players", 1).coerceAtLeast(1)
                    )
                )
            }
        }
        return SquadDashboard(
            played = json.optInt("played", 0).coerceAtLeast(0),
            wins = json.optInt("wins", 0).coerceAtLeast(0),
            accuracy = json.optInt("accuracy", 0).coerceIn(0, 100),
            friendLeaderboard = leaderboard,
            recentMatches = recent
        )
    }

    private fun parseUser(json: JSONObject): SocialUser = SocialUser(
        id = json.getString("id"),
        nickname = json.optString("nickname", "Joueur Cyber"),
        avatarKey = json.optString("avatarKey", "beginner"),
        level = json.optInt("level", 1).coerceAtLeast(1),
        friendCode = json.nullableString("friendCode"),
        email = json.nullableString("email")
    )

    private fun JSONObject.nullableString(key: String): String? =
        if (has(key) && !isNull(key)) getString(key).takeIf { it.isNotBlank() } else null
}
