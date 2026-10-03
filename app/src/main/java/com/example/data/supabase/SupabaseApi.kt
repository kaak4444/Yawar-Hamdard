package com.example.data.supabase

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ResponseBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Small REST boundary for Supabase while Hostinger remains the app's system of record.
 *
 * This deliberately starts with a read-only health check. Auth, Realtime and
 * Storage can be moved behind this boundary incrementally without exposing a
 * service-role key in the APK.
 */
internal interface SupabaseApi {
    @GET("auth/v1/health")
    suspend fun health(
        @Header("apikey") publishableKey: String,
    ): Response<ResponseBody>

    @GET("rest/v1/care_messages")
    suspend fun messages(
        @Header("apikey") publishableKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("conversation_id") conversationFilter: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc",
    ): Response<List<SupabaseMessageDto>>

    @POST("rest/v1/care_messages")
    suspend fun sendMessage(
        @Header("apikey") publishableKey: String,
        @Header("Authorization") bearerToken: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body message: SupabaseMessageInsert,
    ): Response<List<SupabaseMessageDto>>

    @PUT("storage/v1/object/care-attachments/{path}")
    suspend fun uploadAttachment(
        @Path("path", encoded = true) path: String,
        @Header("apikey") publishableKey: String,
        @Header("Authorization") bearerToken: String,
        @Header("Content-Type") contentType: String,
        @Header("x-upsert") upsert: String = "false",
        @Body body: RequestBody,
    ): Response<ResponseBody>

    companion object {
        fun create(): SupabaseApi = Retrofit.Builder()
            .baseUrl("${SupabaseConfig.url}/")
            .addConverterFactory(
                MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build())
            )
            .build()
            .create(SupabaseApi::class.java)
    }
}

internal data class SupabaseMessageDto(
    val id: String,
    val conversation_id: String,
    val sender_id: String,
    val kind: String,
    val body: String,
    val attachment_path: String? = null,
    val attachment_name: String? = null,
    val attachment_mime: String? = null,
    val attachment_bytes: Long? = null,
    val created_at: String,
    val read_at: String? = null,
)

internal data class SupabaseMessageInsert(
    val conversation_id: String,
    val sender_id: String,
    val kind: String,
    val body: String = "",
    val attachment_path: String? = null,
    val attachment_name: String? = null,
    val attachment_mime: String? = null,
    val attachment_bytes: Long? = null,
)
