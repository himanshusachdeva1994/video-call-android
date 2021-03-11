package com.himanshu.videocallsdkvendors.services.twilio

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.himanshu.videocallsdkvendors.helper.twilio.RoomManager
import com.himanshu.videocallsdkvendors.model.twilio.RoomEvent
import com.himanshu.videocallsdkvendors.notifications.ONGOING_NOTIFICATION_ID
import com.himanshu.videocallsdkvendors.notifications.RoomNotification
import com.twilio.video.Room.State.CONNECTED
import com.twilio.video.Room.State.DISCONNECTED
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

/**
 * @author : Himanshu Sachdeva
 * @created : 03-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
class TwilioVideoService : LifecycleService() {

    companion object {
        fun startService(context: Context) {
            Intent(context, TwilioVideoService::class.java).let { intent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stopService(context: Context) {
            Intent(context, TwilioVideoService::class.java).let { context.stopService(it) }
        }
    }

    @Inject
    lateinit var roomManager: RoomManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        roomManager.viewEvents.observe(this, Observer { bindRoomEvents(it) })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("VideoService created")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("VideoService destroyed")
    }

    private fun bindRoomEvents(roomEvent: RoomEvent?) {
        roomEvent?.room?.let { room ->
            if (roomEvent is RoomEvent.RoomState) {
                if (room.state == CONNECTED) {
                    val roomNotification = RoomNotification(this@TwilioVideoService)
                    startForeground(ONGOING_NOTIFICATION_ID,
                            roomNotification.buildNotification(room.name))
                } else if (room.state == DISCONNECTED) {
                    stopSelf()
                }
            }
        }
    }
}