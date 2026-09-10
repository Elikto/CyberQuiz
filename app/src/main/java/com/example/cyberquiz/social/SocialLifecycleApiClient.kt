package com.example.cyberquiz.social

import com.example.cyberquiz.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SocialSentFriendRequest(
    val id: String,
    val to: SocialUser,
    val createdAt: String? = null
)

internal object SocialLifecycleApiClient {
    private val baseUrl = BuildConfig.SOCIAL_API_URL.trimEnd('/').also { value ->
        require(URL(value).protocol.equals("https", ignoreCase = true)) {
            "CyberQuiz social API must use HTTPS"
        }
    }

    suspend fun sentFriendRequests(token: String): List<SocialSentFriendRequest> = withContext(Dispatchers.IO) {
        val array = request("GET", "/friends/requests/sent", token) as JSONArray
        buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    SocialSentFriendRequest(
                        id = item.getString("id"),
                        to = parseUser(item.getJSONObject("to")),
                        createdAt = item.nullableString("createdAt")
                    )
                )
            }
        }
    }

    suspend fun rejectFriendRequest(token: String, requestId: String) = withContext(Dispatchers.IO) {
        request("POST", "/friends/requests/$requestId/reject", token)
        Unit
    }

    suspend fun cancelSentFriendRequest(token: String, requestId: String) = withContext(Dispatchers.IO) {
        request("DELETE", "/friends/requests/$requestId", token)
        Unit
    }

    suspend fun removeFriend(token: String, friendId: String) = withContext(Dispatchers.IO) {
        request("DELETE", "/friends/$friendId", token)
        Unit
    }

    suspend fun rotateFriendCode(token: String): SocialUser = withContext(Dispatchers.IO) {
        parseUser(request("POST", "/me/friend-code/rotate", token) as JSONObject)
    }

    suspend fun changePassword(token: String, currentPassword: String, newPassword: String) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("currentPassword", currentPassword)
            .put("newPassword", newPassword)
        request("POST", "/me/password", token, body)
        Unit
    }

    suspend fun deleteAccount(token: String, currentPassword: String?) = withContext(Dispatchers.IO) {
        val body = JSONObject().put("confirmation", "DELETE")
        if (!currentPassword.isNullOrBlank()) body.put("currentPassword", currentPassword)
        request("DELETE", "/me", token, body)
        Unit
    }

    suspend fun declineRoomInvite(token: String, inviteId: String) = withContext(Dispatchers.IO) {
        request("POST", "/quiz-invites/$inviteId/decline", token)
        Unit
    }

    suspend fun activeRoom(token: String): SocialQuizRoom? = withContext(Dispatchers.IO) {
        val response = request("GET", "/active-room", token)
        if (response == JSONObject.NULL) null else parseRoom(response as JSONObject)
    }

    suspend fun cancelRoom(token: String, roomId: String): SocialQuizRoom = withContext(Dispatchers.IO) {
        parseRoom(request("POST", "/quiz-rooms/$roomId/cancel", token) as JSONObject)
    }

    suspend fun leaveRoom(token: String, roomId: String) = withContext(Dispatchers.IO) {
        request("POST", "/quiz-rooms/$roomId/leave", token)
        Unit
    }

    private fun request(
        method: String,
        path: String,
        token: String,
        body: JSONObject? = null
    ): Any {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "CyberQuiz-Android/${BuildConfig.VERSION_NAME}")
            setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) {
                connection.outputStream.use { stream ->
                    stream.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching {
                    if (text.isBlank()) null else JSONObject(text).optString("detail").takeIf { it.isNotBlank() }
                }.getOrNull() ?: "Le service CyberQuiz a retourné une erreur"
                throw SocialApiException(status, message)
            }
            if (text.isBlank() || text.trim() == "null") return JSONObject.NULL
            val trimmed = text.trimStart()
            return if (trimmed.startsWith("[")) JSONArray(text) else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseUser(json: JSONObject): SocialUser = SocialUser(
        id = json.getString("id"),
        nickname = json.optString("nickname", "Joueur Cyber"),
        avatarKey = json.optString("avatarKey", "beginner"),
        level = json.optInt("level", 1).coerceAtLeast(1),
        friendCode = json.nullableString("friendCode"),
        email = json.nullableString("email")
    )

    private fun parseRoom(json: JSONObject): SocialQuizRoom {
        val questionsJson = json.getJSONArray("questionIds")
        val categoriesJson = json.optJSONArray("categories") ?: JSONArray()
        val membersJson = json.getJSONArray("members")
        val questions = buildList {
            repeat(questionsJson.length()) { index -> add(questionsJson.getLong(index)) }
        }
        val categories = buildList {
            repeat(categoriesJson.length()) { index -> add(categoriesJson.getString(index)) }
        }
        val members = buildList {
            repeat(membersJson.length()) { index ->
                val item = membersJson.getJSONObject(index)
                add(
                    SocialRoomMember(
                        user = parseUser(item),
                        ready = item.optBoolean("ready", false),
                        answered = item.optInt("answered", 0),
                        correct = item.optInt("correct", 0),
                        finished = item.optBoolean("finished", false)
                    )
                )
            }
        }
        return SocialQuizRoom(
            id = json.getString("id"),
            hostUserId = json.getString("hostUserId"),
            status = json.optString("status", "lobby"),
            questionIds = questions,
            mode = json.optString("mode", "RANDOM"),
            categories = categories,
            startsAt = json.nullableString("startsAt"),
            serverNow = json.nullableString("serverNow"),
            members = members
        )
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (has(key) && !isNull(key)) getString(key).takeIf { it.isNotBlank() } else null
}
