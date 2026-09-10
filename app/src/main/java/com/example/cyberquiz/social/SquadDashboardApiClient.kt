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

data class SquadHeadToHeadMatch(
    val roomId: String, val playedAt: String?, val questionCount: Int,
    val myCorrect: Int, val friendCorrect: Int, val outcome: String, val mode: String
)

data class SquadHeadToHead(
    val friend: SocialUser, val played: Int, val wins: Int, val losses: Int, val draws: Int,
    val myAccuracy: Int, val friendAccuracy: Int, val recentMatches: List<SquadHeadToHeadMatch>
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

    suspend fun headToHead(token: String, friendId: String): SquadHeadToHead = withContext(Dispatchers.IO) {
        val url = endpoint.removeSuffix("/dashboard") + "/h2h/" + friendId
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
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
                val message = runCatching { JSONObject(text).optString("detail").takeIf { it.isNotBlank() } }.getOrNull()
                    ?: "Le duel H2H est indisponible"
                throw SocialApiException(status, message)
            }
            parseHeadToHead(JSONObject(text))
        } finally { connection.disconnect() }
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

    internal fun parseHeadToHead(json: JSONObject): SquadHeadToHead {
        val matchesJson = json.optJSONArray("recentMatches") ?: JSONArray()
        val matches = buildList {
            repeat(matchesJson.length()) { index ->
                val item = matchesJson.getJSONObject(index)
                add(SquadHeadToHeadMatch(
                    roomId = item.getString("roomId"),
                    playedAt = item.nullableString("playedAt"),
                    questionCount = item.optInt("questionCount", 0).coerceAtLeast(0),
                    myCorrect = item.optInt("myCorrect", 0).coerceAtLeast(0),
                    friendCorrect = item.optInt("friendCorrect", 0).coerceAtLeast(0),
                    outcome = item.optString("outcome", "draw"),
                    mode = item.optString("mode", "RANDOM")
                ))
            }
        }
        return SquadHeadToHead(
            friend = parseUser(json.getJSONObject("friend")),
            played = json.optInt("played", 0).coerceAtLeast(0),
            wins = json.optInt("wins", 0).coerceAtLeast(0),
            losses = json.optInt("losses", 0).coerceAtLeast(0),
            draws = json.optInt("draws", 0).coerceAtLeast(0),
            myAccuracy = json.optInt("myAccuracy", 0).coerceIn(0, 100),
            friendAccuracy = json.optInt("friendAccuracy", 0).coerceIn(0, 100),
            recentMatches = matches
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
