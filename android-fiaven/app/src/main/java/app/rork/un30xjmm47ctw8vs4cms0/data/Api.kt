package app.rork.un30xjmm47ctw8vs4cms0.data

import app.rork.un30xjmm47ctw8vs4cms0.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiException(message: String, val status: Int) : Exception(message)

val AppJson = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

class Api {
    private val baseUrl: String =
        Config.EXPO_PUBLIC_RORK_FUNCTIONS_URL.ifBlank { "https://fiaven-f2k2lvn-backend.rork.app" }.trimEnd('/')

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .pingInterval(25, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json".toMediaType()

    private suspend fun post(path: String, body: String, token: String? = null): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(baseUrl + path).post(body.toRequestBody(jsonType)).apply {
            if (token != null) header("Authorization", "Bearer $token")
        }.build()
        execute(req)
    }

    private suspend fun get(path: String, token: String?): String = withContext(Dispatchers.IO) {
        execute(Request.Builder().url(baseUrl + path).apply { if (token != null) header("Authorization", "Bearer $token") }.build())
    }

    private fun body(vararg pairs: Pair<String, String>): String = AppJson.encodeToString(
        JsonObject.serializer(),
        JsonObject(pairs.associate { (k, v) -> k to kotlinx.serialization.json.JsonPrimitive(v) }),
    )

    /** Public web pages (also used in the Play Console listing). */
    val privacyUrl: String get() = "$baseUrl/privacidad"
    val deleteAccountUrl: String get() = "$baseUrl/eliminar-cuenta"

    suspend fun recoveryQuestion(username: String): String =
        AppJson.decodeFromString<QuestionResponse>(post("/auth/recover/question", body("username" to username))).question

    suspend fun recover(username: String, answer: String, newPassword: String): AuthResponse =
        AppJson.decodeFromString(post("/auth/recover", body("username" to username, "answer" to answer, "newPassword" to newPassword)))

    suspend fun security(token: String): SecurityInfo = AppJson.decodeFromString(get("/account/security", token))

    suspend fun setSecurity(token: String, password: String, question: String, answer: String) {
        post("/account/security", body("password" to password, "question" to question, "answer" to answer), token)
    }

    suspend fun updateAccount(token: String, password: String, username: String, displayName: String, newPassword: String): AuthResponse =
        AppJson.decodeFromString(
            post(
                "/account/update",
                body("password" to password, "username" to username, "displayName" to displayName, "newPassword" to newPassword),
                token,
            )
        )

    suspend fun updateMember(token: String, username: String, displayName: String, newPassword: String) {
        post("/team/update", body("username" to username, "displayName" to displayName, "newPassword" to newPassword), token)
    }

    suspend fun deleteAccount(token: String, password: String) {
        post("/account/delete", body("password" to password), token)
    }

    suspend fun credits(token: String?): CreditsResponse = AppJson.decodeFromString(get("/credits", token))

    suspend fun setCredits(token: String, credits: Credits) {
        val payload = AppJson.encodeToString(
            JsonObject.serializer(),
            JsonObject(mapOf("credits" to AppJson.encodeToJsonElement(Credits.serializer(), credits))),
        )
        post("/credits", payload, token)
    }

    private fun execute(req: Request): String {
        client.newCall(req).execute().use { res ->
            val text = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                val msg = runCatching { AppJson.decodeFromString<ErrorResponse>(text).error }.getOrNull()
                throw ApiException(msg?.ifBlank { null } ?: "Error del servidor (${res.code})", res.code)
            }
            return text
        }
    }

    suspend fun register(
        businessName: String, displayName: String, username: String, password: String, question: String, answer: String,
    ): AuthResponse = AppJson.decodeFromString(
        post(
            "/auth/register",
            body(
                "businessName" to businessName, "displayName" to displayName, "username" to username,
                "password" to password, "question" to question, "answer" to answer,
            ),
        )
    )

    suspend fun login(username: String, password: String): AuthResponse {
        val body = AppJson.encodeToString(
            JsonObject.serializer(),
            JsonObject(
                mapOf(
                    "username" to kotlinx.serialization.json.JsonPrimitive(username),
                    "password" to kotlinx.serialization.json.JsonPrimitive(password),
                )
            )
        )
        return AppJson.decodeFromString(post("/auth/login", body))
    }

    suspend fun logout(token: String) {
        runCatching { post("/auth/logout", "{}", token) }
    }

    suspend fun sync(token: String, request: SyncRequest): SyncResponse =
        AppJson.decodeFromString(post("/sync", AppJson.encodeToString(SyncRequest.serializer(), request), token))

    suspend fun team(token: String): List<TeamMember> =
        AppJson.decodeFromString<TeamResponse>(get("/team", token)).users

    suspend fun addMember(token: String, displayName: String, username: String, password: String, title: String = "") {
        post("/team/add", body("displayName" to displayName, "username" to username, "password" to password, "title" to title), token)
    }

    suspend fun setTitle(token: String, username: String, title: String) {
        post("/team/title", body("username" to username, "title" to title), token)
    }

    suspend fun me(token: String): MeResponse = AppJson.decodeFromString(get("/me", token))

    /** Uploads a JPEG (base64) once; the server never overwrites an existing id. */
    suspend fun uploadAttachment(token: String, id: String, base64: String) {
        post("/attachment", body("id" to id, "data" to base64), token)
    }

    suspend fun downloadAttachment(token: String, id: String): String =
        AppJson.parseToJsonElement(get("/attachment/$id", token)).jsonObject["data"]?.jsonPrimitive?.content
            ?: throw IOException("Sin datos")

    suspend fun removeMember(token: String, username: String) {
        val body = AppJson.encodeToString(
            JsonObject.serializer(),
            JsonObject(mapOf("username" to kotlinx.serialization.json.JsonPrimitive(username)))
        )
        post("/team/remove", body, token)
    }

    /** Official BCV average rate from a public Venezuelan exchange API. */
    suspend fun fetchBcvRate(): Pair<Double, String> = withContext(Dispatchers.IO) {
        val req = Request.Builder().url("https://ve.dolarapi.com/v1/dolares/oficial").build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IOException("BCV ${res.code}")
            val obj = AppJson.parseToJsonElement(res.body?.string().orEmpty()).jsonObject
            val rate = obj["promedio"]?.jsonPrimitive?.double ?: throw IOException("Sin tasa")
            val date = obj["fechaActualizacion"]?.jsonPrimitive?.content.orEmpty()
            rate to date
        }
    }

    fun openSocket(token: String, listener: WebSocketListener): WebSocket {
        val wsUrl = baseUrl.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://") + "/ws?token=$token"
        return client.newWebSocket(Request.Builder().url(wsUrl).build(), listener)
    }
}
