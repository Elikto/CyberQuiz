package com.example.cyberquiz.social

import com.example.cyberquiz.BuildConfig
import com.example.cyberquiz.model.EngagementMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal object EconomyApiClient {
    private val baseUrl = (BuildConfig.SOCIAL_API_URL.trimEnd('/') + "/economy").also { value ->
        require(URL(value).protocol.equals("https", ignoreCase = true)) {
            "CyberQuiz economy API must use HTTPS"
        }
    }

    suspend fun sync(
        token: String,
        metrics: EngagementMetrics,
        seed: EconomySeed
    ): EconomyState = withContext(Dispatchers.IO) {
        parseState(request("POST", "/sync", token, syncBody(metrics, seed)))
    }

    suspend fun claimMission(
        token: String,
        missionId: String,
        metrics: EngagementMetrics,
        seed: EconomySeed
    ): EconomyState = withContext(Dispatchers.IO) {
        parseState(request("POST", "/missions/$missionId/claim", token, syncBody(metrics, seed)))
    }

    suspend fun claimLevel(
        token: String,
        level: Int,
        metrics: EngagementMetrics,
        seed: EconomySeed
    ): EconomyState = withContext(Dispatchers.IO) {
        parseState(request("POST", "/levels/$level/claim", token, syncBody(metrics, seed)))
    }

    suspend fun purchase(
        token: String,
        kind: String,
        storageKey: String,
        metrics: EngagementMetrics,
        seed: EconomySeed
    ): EconomyState = withContext(Dispatchers.IO) {
        val body = syncBody(metrics, seed)
            .put("kind", kind)
            .put("storageKey", storageKey)
        parseState(request("POST", "/purchase", token, body))
    }

    private fun syncBody(metrics: EngagementMetrics, seed: EconomySeed): JSONObject = JSONObject().apply {
        put("metrics", JSONObject().apply {
            put("answered", metrics.answered.coerceAtLeast(0))
            put("correct", metrics.correct.coerceIn(0, metrics.answered.coerceAtLeast(0)))
            put("xp", metrics.xp.coerceAtLeast(0))
            put("level", metrics.level.coerceIn(1, 30))
            put("streak", metrics.streak.coerceAtLeast(0))
            put("bestStreak", metrics.bestStreak.coerceAtLeast(0))
            put("quizCount", metrics.quizCount.coerceAtLeast(0))
        })
        put("seed", JSONObject().apply {
            put("coins", seed.coins.coerceIn(0, 1_000_000))
            putNullable("loginDay", seed.loginDay)
            put("loginStreak", seed.loginStreak.coerceAtLeast(0))
            put("loginRewardToday", seed.loginRewardToday.coerceAtLeast(0))
            putNullable("baselineDay", seed.baselineDay)
            put("baselineAnswered", seed.baselineAnswered.coerceAtLeast(0))
            put("baselineCorrect", seed.baselineCorrect.coerceIn(0, seed.baselineAnswered.coerceAtLeast(0)))
            put("baselineXp", seed.baselineXp.coerceAtLeast(0))
            put("claimedMissionIds", seed.claimedMissionIds.toJsonArray())
            put("unlockedAchievementIds", seed.unlockedAchievementIds.toJsonArray())
            put("claimedLevels", JSONArray().apply { seed.claimedLevels.sorted().forEach(::put) })
            put("purchasedFrameKeys", seed.purchasedFrameKeys.toJsonArray())
            put("purchasedAvatarKeys", seed.purchasedAvatarKeys.toJsonArray())
            put("purchasedBannerKeys", seed.purchasedBannerKeys.toJsonArray())
        })
    }

    private fun request(method: String, path: String, token: String, body: JSONObject): JSONObject {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8_000
            readTimeout = 12_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("User-Agent", "CyberQuiz-Android/${BuildConfig.VERSION_NAME}")
        }
        try {
            connection.outputStream.use { stream ->
                stream.write(body.toString().toByteArray(Charsets.UTF_8))
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching {
                    JSONObject(text).optString("detail").takeIf { it.isNotBlank() }
                }.getOrNull() ?: "Le service CyberQuiz a retourné une erreur"
                throw SocialApiException(status, message)
            }
            return JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseState(json: JSONObject) = EconomyState(
        coins = json.optInt("coins", 0).coerceAtLeast(0),
        loginDay = json.nullableString("loginDay"),
        loginStreak = json.optInt("loginStreak", 0).coerceAtLeast(0),
        loginRewardToday = json.optInt("loginRewardToday", 0).coerceAtLeast(0),
        baselineDay = json.nullableString("baselineDay"),
        baselineAnswered = json.optInt("baselineAnswered", 0).coerceAtLeast(0),
        baselineCorrect = json.optInt("baselineCorrect", 0).coerceAtLeast(0),
        baselineXp = json.optInt("baselineXp", 0).coerceAtLeast(0),
        claimedMissionIds = json.strings("claimedMissionIds").toSet(),
        unlockedAchievementIds = json.strings("unlockedAchievementIds").toSet(),
        claimedLevels = json.ints("claimedLevels").filter { it in 1..30 }.toSet(),
        purchasedFrameKeys = json.strings("purchasedFrameKeys").toSet(),
        purchasedAvatarKeys = json.strings("purchasedAvatarKeys").toSet(),
        purchasedBannerKeys = json.strings("purchasedBannerKeys").toSet(),
        updatedAt = json.nullableString("updatedAt")
    )

    private fun JSONObject.putNullable(key: String, value: String?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun Set<String>.toJsonArray(): JSONArray = JSONArray().apply {
        this@toJsonArray.sorted().forEach(::put)
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (has(key) && !isNull(key)) optString(key).takeIf { it.isNotBlank() } else null

    private fun JSONObject.strings(key: String): List<String> {
        val array = optJSONArray(key) ?: return emptyList()
        return buildList {
            repeat(array.length()) { index ->
                array.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private fun JSONObject.ints(key: String): List<Int> {
        val array = optJSONArray(key) ?: return emptyList()
        return buildList {
            repeat(array.length()) { index -> add(array.optInt(index, -1)) }
        }
    }
}
