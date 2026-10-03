package com.example.data.supabase

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Lightweight Supabase Realtime v1 client for care_messages changes.
 *
 * The caller supplies the Supabase Auth access token. The publishable key is
 * only a routing key; RLS still decides which rows can be delivered.
 */
internal class SupabaseRealtimeClient(
    private val accessToken: String,
    private val onMessage: (String) -> Unit,
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var socket: WebSocket? = null
    private var ref = 0

    fun connect() {
        if (socket != null) return
        val endpoint = SupabaseConfig.url
            .removePrefix("https://")
            .removePrefix("http://")
        val request = Request.Builder()
            .url("wss://$endpoint/realtime/v1/websocket?apikey=${SupabaseConfig.publishableKey}&vsn=1.0.0")
            .build()
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val joinRef = nextRef()
                webSocket.send(JSONObject().apply {
                    put("topic", "realtime:care_messages")
                    put("event", "phx_join")
                    put("payload", JSONObject().apply {
                        put("config", JSONObject().apply {
                            put("broadcast", JSONObject().put("ack", false))
                            put("presence", JSONObject().put("key", ""))
                            put("postgres_changes", JSONArray().put(JSONObject().apply {
                                put("event", "*")
                                put("schema", "public")
                                put("table", "care_messages")
                            }))
                        })
                        put("access_token", accessToken)
                    })
                    put("ref", joinRef)
                }.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.contains("\"event\":\"postgres_changes\"")) onMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                socket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                socket = null
            }
        })
    }

    fun sendHeartbeat() {
        socket?.send(JSONObject().apply {
            put("topic", "phoenix")
            put("event", "heartbeat")
            put("payload", JSONObject())
            put("ref", nextRef())
        }.toString())
    }

    fun close() {
        socket?.close(1000, "closed")
        socket = null
        client.dispatcher.executorService.shutdown()
    }

    private fun nextRef(): String = (++ref).toString()
}
