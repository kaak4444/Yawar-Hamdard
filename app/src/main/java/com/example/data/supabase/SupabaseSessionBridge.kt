package com.example.data.supabase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Exchanges the existing, server-verified Hostinger session for a Supabase JWT.
 * No password or service-role key is stored or transmitted by this client.
 */
internal class SupabaseSessionBridge {
    private val client = OkHttpClient.Builder().callTimeout(25, TimeUnit.SECONDS).build()

    suspend fun exchange(hostingerToken: String): SupabaseSession = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(SupabaseConfig.url + "/functions/v1/hostinger-session")
            .header("apikey", SupabaseConfig.publishableKey)
            .header("Authorization", "Bearer $hostingerToken")
            .post(ByteArray(0).toRequestBody())
            .build()
        client.newCall(request).execute().use { response ->
            val body = JSONObject(response.body?.string() ?: "{}")
            if (!response.isSuccessful) throw IOException(body.optString("error", "Messaging sign-in failed"))
            val accessToken = body.getString("access_token")
            val userId = body.getString("user_id")
            require(accessToken.isNotBlank() && userId.isNotBlank())
            SupabaseSession(accessToken, userId, body.getLong("expires_at"))
        }
    }
}

internal data class SupabaseSession(val accessToken: String, val userId: String, val expiresAt: Long)
