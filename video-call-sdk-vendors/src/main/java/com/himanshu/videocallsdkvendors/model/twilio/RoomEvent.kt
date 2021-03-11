package com.himanshu.videocallsdkvendors.model.twilio

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
sealed class RoomEvent(val room: Room? = null) {
    object Connecting : RoomEvent()
    class RoomState(room: Room) : RoomEvent(room)
    class ConnectFailure(room: Room) : RoomEvent(room)
    class ParticipantConnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class ParticipantDisconnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class DominantSpeakerChanged(room: Room, val remoteParticipant: RemoteParticipant?) : RoomEvent(room)
}
