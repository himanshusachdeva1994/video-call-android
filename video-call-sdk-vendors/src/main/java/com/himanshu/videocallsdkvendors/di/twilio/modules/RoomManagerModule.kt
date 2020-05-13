package com.himanshu.videocallsdkvendors.di.twilio.modules

import android.app.Application
import com.himanshu.videocallsdkvendors.di.twilio.ApplicationModule
import com.himanshu.videocallsdkvendors.di.twilio.ApplicationScope
import com.himanshu.videocallsdkvendors.helper.twilio.RoomManager
import dagger.Module
import dagger.Provides

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
@Module(includes = [ApplicationModule::class])
class RoomManagerModule {

    @Provides
    @ApplicationScope
    fun providesRoomManager(application: Application): RoomManager {
        return RoomManager(application)
    }
}