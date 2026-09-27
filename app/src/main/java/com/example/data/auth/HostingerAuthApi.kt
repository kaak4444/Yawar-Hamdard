package com.example.data.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.IOException
import org.json.JSONObject
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class AuthApiResult(
    val ok: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val token: String? = null,
    val user: AuthApiUser? = null
)

internal data class AuthApiUser(
    val id: Long,
    val email: String,
    val role: String
)

internal data class CredentialsRequest(val email: String, val password: String)
internal data class EmailRequest(val email: String)
internal data class CodeRequest(val email: String, val code: String)
internal data class PasswordResetRequest(val email: String, val code: String, val password: String)

internal interface HostingerAuthApi {
    @POST("auth.php?action=signup")
    suspend fun signUp(@Body request: CredentialsRequest): Response<AuthApiResult>

    @POST("auth.php?action=verify-signup")
    suspend fun verifySignUp(@Body request: CodeRequest): Response<AuthApiResult>

    @POST("auth.php?action=resend-signup")
    suspend fun resendSignUpCode(@Body request: EmailRequest): Response<AuthApiResult>

    @POST("auth.php?action=login")
    suspend fun signIn(@Body request: CredentialsRequest): Response<AuthApiResult>

    @POST("auth.php?action=password-reset-start")
    suspend fun startPasswordReset(@Body request: EmailRequest): Response<AuthApiResult>

    @POST("auth.php?action=password-reset")
    suspend fun resetPassword(@Body request: PasswordResetRequest): Response<AuthApiResult>

    @GET("auth.php?action=me")
    suspend fun currentUser(@Header("Authorization") authorization: String): Response<AuthApiResult>

    @POST("auth.php?action=logout")
    suspend fun signOut(@Header("Authorization") authorization: String): Response<AuthApiResult>

    companion object {
        fun create(): HostingerAuthApi {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://yawarconsulting.com/api/v1/")
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(HostingerAuthApi::class.java)
        }
    }
}

internal fun <T> Response<T>.requireSuccessfulBody(): T {
    val body = body()
    if (!isSuccessful) {
        val rawError = errorBody()?.use { it.string() }.orEmpty()
        val message = runCatching { JSONObject(rawError).optString("error") }
            .getOrNull()
            ?.takeIf(String::isNotBlank)
            ?: "The request could not be completed. Please try again."
        throw AuthApiException(code(), message)
    }
    if (body == null) throw IOException("The server returned an empty response.")
    if (body is AuthApiResult && !body.ok) {
        throw AuthApiException(code(), body.error ?: "The request could not be completed. Please try again.")
    }
    return body
}

internal class AuthApiException(val statusCode: Int, message: String) : IOException(message)

/** Stores the session token encrypted with an AES key held by Android Keystore. */
internal class AuthTokenStore(context: android.content.Context) {
    private val preferences = context.getSharedPreferences("hostinger_session", android.content.Context.MODE_PRIVATE)

    fun read(): String? {
        val stored = preferences.getString(TOKEN_KEY, null) ?: return null
        return try {
            val parts = stored.split(':', limit = 2)
            if (parts.size != 2) return clearAndReturnNull()
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (_: Exception) {
            clearAndReturnNull()
        }
    }

    fun write(token: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val value = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        preferences.edit().putString(TOKEN_KEY, value).apply()
    }

    fun clear() {
        preferences.edit().remove(TOKEN_KEY).apply()
    }

    private fun clearAndReturnNull(): String? {
        clear()
        return null
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val TOKEN_KEY = "encrypted_access_token"
        const val KEY_ALIAS = "yawar_hostinger_session_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
