package com.himanshu.videocallsdkvendors.viewmodel

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.himanshu.videocallsdkvendors.R
import com.himanshu.videocallsdkvendors.helper.twilio.RoomManager
import com.himanshu.videocallsdkvendors.model.twilio.RoomEvent
import com.himanshu.videocallsdkvendors.viewmodel.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
class TwilioVideoCallViewModel(application: Application, private val roomManager: RoomManager) : BaseViewModel(application) {

    val identity: String = "Himanshu"
    val roomName: String = "room1"

    val roomEvents: LiveData<RoomEvent?> = roomManager.viewEvents

    fun connectToRoom(authToken: String) =
            viewModelScope.launch {
                roomManager.connectToRoom(
                        identity,
                        roomName,
                        authToken,
                        false)
            }

    private fun disconnect() {
        roomManager.disconnect()
    }

    override fun onClicked(view: View?) {
        when (view?.id) {
            R.id.disconnect -> {
                disconnect()
                super.onClicked(view)
            }
            else -> {
                super.onClicked(view)
            }
        }
    }

    override fun setRepositoryToastMessage(): LiveData<String>? {
        return null
    }
}