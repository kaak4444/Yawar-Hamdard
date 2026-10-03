package com.example.data.supabase

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header

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

    companion object {
        fun create(): SupabaseApi = Retrofit.Builder()
            .baseUrl("${SupabaseConfig.url}/")
            .build()
            .create(SupabaseApi::class.java)
    }
}
