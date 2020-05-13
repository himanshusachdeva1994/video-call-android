package com.himanshu.videocallsdkvendors.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.himanshu.videocallsdkvendors.helper.twilio.RoomManager
import com.himanshu.videocallsdkvendors.viewmodel.TwilioVideoCallViewModel

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva@instantsys.com
 */
class RoomViewModelFactory(private val application: Application,
                           private val roomManager: RoomManager)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TwilioVideoCallViewModel(application, roomManager) as T
    }
}