package com.example.ui.workflow

import android.content.Context
import android.media.AudioManager
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.json.JSONObject
import com.example.data.auth.WorkflowIceServer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Thin audio-only WebRTC wrapper. Signaling stays in the authenticated Hostinger API. */
internal class WebRtcVoiceEngine(
    context: Context,
    iceServers: List<WorkflowIceServer>,
    private val onIceCandidate: (String) -> Unit,
    private val onConnectionState: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val factory: PeerConnectionFactory
    private val peer: PeerConnection
    private val audioSource: AudioSource
    private val audioTrack: AudioTrack
    private val pendingCandidates = mutableListOf<IceCandidate>()
    private var hasRemoteDescription = false
    private var released = false

    init {
        if (initialized.compareAndSet(false, true)) {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext).createInitializationOptions()
            )
        }
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()
        val rtcServers = iceServers.flatMap { server ->
            server.urls.map { url ->
                val builder = PeerConnection.IceServer.builder(url)
                if (server.username.isNotBlank()) builder.setUsername(server.username)
                if (server.credential.isNotBlank()) builder.setPassword(server.credential)
                builder.createIceServer()
            }
        }
        val rtcConfig = PeerConnection.RTCConfiguration(rtcServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        peer = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState) = Unit
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                if (state == PeerConnection.IceConnectionState.FAILED) onConnectionState("Call could not connect on this network")
                if (state == PeerConnection.IceConnectionState.DISCONNECTED) onConnectionState("Connection interrupted")
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) = Unit
            override fun onIceCandidate(candidate: IceCandidate) {
                onIceCandidate(JSONObject().put("sdpMid", candidate.sdpMid).put("sdpMLineIndex", candidate.sdpMLineIndex)
                    .put("candidate", candidate.sdp).toString())
            }
            override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) = Unit
            override fun onAddStream(stream: org.webrtc.MediaStream) = Unit
            override fun onRemoveStream(stream: org.webrtc.MediaStream) = Unit
            override fun onDataChannel(channel: org.webrtc.DataChannel) = Unit
            override fun onRenegotiationNeeded() = Unit
            override fun onAddTrack(receiver: org.webrtc.RtpReceiver, mediaStreams: Array<org.webrtc.MediaStream>) = Unit
            override fun onConnectionChange(state: PeerConnection.PeerConnectionState) {
                onConnectionState(when (state) {
                    PeerConnection.PeerConnectionState.CONNECTED -> "Connected"
                    PeerConnection.PeerConnectionState.CONNECTING -> "Connecting…"
                    PeerConnection.PeerConnectionState.DISCONNECTED -> "Connection interrupted"
                    PeerConnection.PeerConnectionState.FAILED -> "Call could not connect on this network"
                    PeerConnection.PeerConnectionState.CLOSED -> "Call ended"
                    else -> "Setting up secure call…"
                })
            }
        }) ?: throw IllegalStateException("WebRTC could not create a peer connection.")
        audioSource = factory.createAudioSource(MediaConstraints())
        audioTrack = factory.createAudioTrack("yawar-audio", audioSource)
        peer.addTrack(audioTrack, listOf("yawar-audio"))
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false
    }

    suspend fun createOffer(): String {
        val description = suspendCancellableCoroutine<SessionDescription> { continuation ->
            peer.createOffer(object : SdpObserver {
                override fun onCreateSuccess(value: SessionDescription) { if (continuation.isActive) continuation.resume(value) }
                override fun onSetSuccess() = Unit
                override fun onCreateFailure(error: String) { if (continuation.isActive) continuation.resumeWithException(IllegalStateException(error)) }
                override fun onSetFailure(error: String) = Unit
            }, MediaConstraints())
        }
        setLocalDescription(description)
        return description.description
    }

    suspend fun acceptOffer(offer: String): String {
        setRemoteDescription(SessionDescription(SessionDescription.Type.OFFER, offer))
        val answer = suspendCancellableCoroutine<SessionDescription> { continuation ->
            peer.createAnswer(object : SdpObserver {
                override fun onCreateSuccess(value: SessionDescription) { if (continuation.isActive) continuation.resume(value) }
                override fun onSetSuccess() = Unit
                override fun onCreateFailure(error: String) { if (continuation.isActive) continuation.resumeWithException(IllegalStateException(error)) }
                override fun onSetFailure(error: String) = Unit
            }, MediaConstraints())
        }
        setLocalDescription(answer)
        return answer.description
    }

    suspend fun acceptAnswer(answer: String) {
        setRemoteDescription(SessionDescription(SessionDescription.Type.ANSWER, answer))
    }

    fun addRemoteCandidate(json: String) {
        runCatching {
            val value = JSONObject(json)
            val candidate = IceCandidate(value.optString("sdpMid"), value.optInt("sdpMLineIndex"), value.getString("candidate"))
            if (hasRemoteDescription) peer.addIceCandidate(candidate) else pendingCandidates += candidate
        }
    }

    fun setMuted(muted: Boolean) { audioTrack.setEnabled(!muted) }

    fun setSpeaker(enabled: Boolean) { audioManager.isSpeakerphoneOn = enabled }

    fun close() {
        if (released) return
        released = true
        runCatching { audioTrack.setEnabled(false); audioTrack.dispose() }
        runCatching { audioSource.dispose() }
        runCatching { peer.close(); peer.dispose() }
        runCatching { factory.dispose() }
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    private suspend fun setLocalDescription(description: SessionDescription) {
        suspendCancellableCoroutine<Unit> { continuation ->
            peer.setLocalDescription(object : SdpObserver {
                override fun onCreateSuccess(value: SessionDescription) = Unit
                override fun onSetSuccess() { if (continuation.isActive) continuation.resume(Unit) }
                override fun onCreateFailure(error: String) = Unit
                override fun onSetFailure(error: String) { if (continuation.isActive) continuation.resumeWithException(IllegalStateException(error)) }
            }, description)
        }
    }

    private suspend fun setRemoteDescription(description: SessionDescription) {
        suspendCancellableCoroutine<Unit> { continuation ->
            peer.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(value: SessionDescription) = Unit
                override fun onSetSuccess() {
                    hasRemoteDescription = true
                    pendingCandidates.forEach(peer::addIceCandidate)
                    pendingCandidates.clear()
                    if (continuation.isActive) continuation.resume(Unit)
                }
                override fun onCreateFailure(error: String) = Unit
                override fun onSetFailure(error: String) { if (continuation.isActive) continuation.resumeWithException(IllegalStateException(error)) }
            }, description)
        }
    }

    private companion object {
        val initialized = AtomicBoolean(false)
    }
}
