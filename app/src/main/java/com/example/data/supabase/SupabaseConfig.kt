package com.example.data.supabase

import com.example.BuildConfig

/** Public Supabase settings used by the Android client.
 *
 * The publishable key is intentionally public and is protected by Supabase
 * Row Level Security. The secret/service-role key must stay on a server and
 * is never read by this class.
 */
internal object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL.trimEnd('/')
    val publishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    val jwksUrl: String = BuildConfig.SUPABASE_JWKS_URL

    val isConfigured: Boolean
        get() = url.startsWith("https://") &&
            publishableKey.isNotBlank() &&
            !publishableKey.startsWith("replace_with_")
}
