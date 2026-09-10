package com.example.cyberquiz.social

import com.example.cyberquiz.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal class SocialApiException(
    val statusCode: Int,
    override val message: String
) : Exception(message)

internal object SocialApiClient {
    private val baseUrl = BuildConfig.SOCIAL_API_URL.trimEnd('/').also { value ->
        require(URL(value).protocol.equals("https", ignoreCase = true)) {
            "CyberQuiz social API must use HTTPS"
        }
    }

    suspend fun config(): SocialConfig = withContext(Dispatchers.IO) {
        val json = request("GET", "/config") as JSONObject
        SocialConfig(json.nullableString("googleClientId"))
    }

    suspend fun register(
        email: String,
        password: String,
        nickname: String,
        avatarKey: String,
        level: Int
    ): SocialSession = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("nickname", nickname)
            .put("avatarKey", avatarKey)
            .put("level", level)
        parseSession(request("POST", "/auth/register", body = body) as JSONObject)
    }

    suspend fun login(email: String, password: String): SocialSession = withContext(Dispatchers.IO) {
        val body = JSONObject().put("email", email).put("password", password)
        parseSession(request("POST", "/auth/login", body = body) as JSONObject)
    }

    suspend fun googleLogin(
        idToken: String,
        nickname: String,
        avatarKey: String,
        level: Int
    ): SocialSession = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("idToken", idToken)
            .put("nickname", nickname)
            .put("avatarKey", avatarKey)
            .put("level", level)
        parseSession(request("POST", "/auth/google", body = body) as JSONObject)
    }

    suspend fun me(token: String): SocialUser = withContext(Dispatchers.IO) {
        parseUser(request("GET", "/me", token = token) as JSONObject)
    }

    suspend fun updateProfile(
        token: String,
        nickname: String,
        avatarKey: String,
        level: Int
    ): SocialUser = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("nickname", nickname)
            .put("avatarKey", avatarKey)
            .put("level", level)
        parseUser(request("PUT", "/me/profile", token, body) as JSONObject)
    }

    suspend fun friends(token: String): List<SocialUser> = withContext(Dispatchers.IO) {
        parseUsers(request("GET", "/friends", token) as JSONArray)
    }

    suspend fun friendRequests(token: String): List<SocialFriendRequest> = withContext(Dispatchers.IO) {
        val array = request("GET", "/friends/requests", token) as JSONArray
        buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    SocialFriendRequest(
                        id = item.getString("id"),
                        from = parseUser(item.getJSONObject("from")),
                        createdAt = item.nullableString("createdAt")
                    )
                )
            }
        }
    }

    suspend fun addFriend(token: String, friendCode: String) = withContext(Dispatchers.IO) {
        request(
            "POST",
            "/friends/requests",
            token,
            JSONObject().put("friendCode", friendCode.trim().uppercase())
        )
        Unit
    }

    suspend fun acceptFriendRequest(token: String, requestId: String): SocialUser = withContext(Dispatchers.IO) {
        val json = request("POST", "/friends/requests/$requestId/accept", token) as JSONObject
        parseUser(json.getJSONObject("friend"))
    }

    suspend fun roomInvites(token: String): List<SocialQuizInvite> = withContext(Dispatchers.IO) {
        val array = request("GET", "/quiz-invites", token) as JSONArray
        buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    SocialQuizInvite(
                        id = item.getString("id"),
                        roomId = item.getString("roomId"),
                        from = parseUser(item.getJSONObject("from")),
                        mode = item.optString("mode", "RANDOM"),
                        createdAt = item.nullableString("createdAt")
                    )
                )
            }
        }
    }

    suspend fun createRoom(
        token: String,
        inviteeIds: List<String>,
        questionIds: List<Long>,
        mode: String,
        categories: List<String>
    ): SocialQuizRoom = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("inviteeIds", JSONArray(inviteeIds))
            .put("questionIds", JSONArray(questionIds))
            .put("mode", mode)
            .put("categories", JSONArray(categories))
        parseRoom(request("POST", "/quiz-rooms", token, body) as JSONObject)
    }

    suspend fun acceptRoomInvite(token: String, inviteId: String): SocialQuizRoom = withContext(Dispatchers.IO) {
        parseRoom(request("POST", "/quiz-invites/$inviteId/accept", token) as JSONObject)
    }

    suspend fun room(token: String, roomId: String): SocialQuizRoom = withContext(Dispatchers.IO) {
        parseRoom(request("GET", "/quiz-rooms/$roomId", token) as JSONObject)
    }

    suspend fun setReady(token: String, roomId: String, ready: Boolean): SocialQuizRoom = withContext(Dispatchers.IO) {
        parseRoom(
            request(
                "POST",
                "/quiz-rooms/$roomId/ready",
                token,
                JSONObject().put("ready", ready)
            ) as JSONObject
        )
    }

    suspend fun startRoom(token: String, roomId: String): SocialQuizRoom = withContext(Dispatchers.IO) {
        parseRoom(request("POST", "/quiz-rooms/$roomId/start", token) as JSONObject)
    }

    suspend fun updateRoomProgress(
        token: String,
        roomId: String,
        answered: Int,
        correct: Int,
        finished: Boolean
    ): SocialQuizRoom = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("answered", answered)
            .put("correct", correct)
            .put("finished", finished)
        parseRoom(request("POST", "/quiz-rooms/$roomId/progress", token, body) as JSONObject)
    }

    private fun request(
        method: String,
        path: String,
        token: String? = null,
        body: JSONObject? = null
    ): Any {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "CyberQuiz-Android/${BuildConfig.VERSION_NAME}")
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
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
            if (text.isBlank()) return JSONObject()
            val trimmed = text.trimStart()
            return if (trimmed.startsWith("[")) JSONArray(text) else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseSession(json: JSONObject): SocialSession = SocialSession(
        token = json.getString("token"),
        user = parseUser(json.getJSONObject("user"))
    )

    private fun parseUsers(array: JSONArray): List<SocialUser> = buildList {
        repeat(array.length()) { index -> add(parseUser(array.getJSONObject(index))) }
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
