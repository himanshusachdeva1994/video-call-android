package com.himanshu.videocallsdkvendors.helper.twilio

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.himanshu.videocallsdkvendors.model.twilio.RoomEvent
import com.himanshu.videocallsdkvendors.services.twilio.TwilioVideoService.Companion.startService
import com.himanshu.videocallsdkvendors.services.twilio.TwilioVideoService.Companion.stopService
import com.twilio.video.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
class RoomManager(private val context: Context,
                  private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    var room: Room? = null
    private val mutableViewEvents: MutableLiveData<RoomEvent?> = MutableLiveData()

    val viewEvents: LiveData<RoomEvent?> = mutableViewEvents

    private val roomListener = RoomListener()

    fun disconnect() {
        room?.disconnect()
    }

    suspend fun connectToRoom(
            identity: String,
            roomName: String,
            authToken: String,
            isNetworkQualityEnabled: Boolean
    ) {
        coroutineScope.launch {
            try {
                mutableViewEvents.postValue(RoomEvent.Connecting)

                val enableInsights = true
                val enableAutomaticTrackSubscription = true
                val enableDominantSpeaker = true
                val preferredVideoCodec: VideoCodec = Vp8Codec()
                val preferredAudioCodec: AudioCodec = OpusCodec()

                val configuration = NetworkQualityConfiguration(
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL)

                val connectOptionsBuilder = ConnectOptions.Builder(authToken)
                        .roomName(roomName)
                        .enableAutomaticSubscription(enableAutomaticTrackSubscription)
                        .enableDominantSpeaker(enableDominantSpeaker)
                        .enableInsights(enableInsights)
                        .enableNetworkQuality(isNetworkQualityEnabled)
                        .networkQualityConfiguration(configuration)

                connectOptionsBuilder.preferVideoCodecs(listOf(preferredVideoCodec))
                connectOptionsBuilder.preferAudioCodecs(listOf(preferredAudioCodec))

                val room = Video.connect(
                        context,
                        connectOptionsBuilder.build(),
                        roomListener)
                this@RoomManager.room = room
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class RoomListener : Room.Listener {
        override fun onConnected(room: Room) {
            Timber.i("onConnected -> room sid: %s",
                    room.sid)

            startService(context)

            // Reset the speakerphone
            mutableViewEvents.value = RoomEvent.RoomState(room)
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            Timber.i("Disconnected from room -> sid: %s, state: %s",
                    room.sid, room.state)

            stopService(context)

            mutableViewEvents.value = RoomEvent.RoomState(room)
        }

        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            Timber.e(
                    "Failed to connect to room -> sid: %s, state: %s, code: %d, error: %s",
                    room.sid,
                    room.state,
                    twilioException.code,
                    twilioException.message)
            mutableViewEvents.value = RoomEvent.ConnectFailure(room)
        }

        override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant connected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)
            mutableViewEvents.value = RoomEvent.ParticipantConnected(room, remoteParticipant)
        }

        override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
            Timber.i("RemoteParticipant disconnected -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant.sid)
            mutableViewEvents.value = RoomEvent.ParticipantDisconnected(room, remoteParticipant)
        }

        override fun onDominantSpeakerChanged(room: Room, remoteParticipant: RemoteParticipant?) {
            Timber.i("DominantSpeakerChanged -> room sid: %s, remoteParticipant: %s",
                    room.sid, remoteParticipant?.sid)
            mutableViewEvents.value = RoomEvent.DominantSpeakerChanged(room, remoteParticipant)
        }

        override fun onRecordingStarted(room: Room) {}

        override fun onReconnected(room: Room) {
            Timber.i("onReconnected: %s", room.name)
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            Timber.i("onReconnecting: %s", room.name)
        }

        override fun onRecordingStopped(room: Room) {}
    }
}