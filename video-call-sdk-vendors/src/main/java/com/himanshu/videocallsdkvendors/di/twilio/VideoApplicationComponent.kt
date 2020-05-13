package com.himanshu.videocallsdkvendors.di.twilio

import com.himanshu.videocallsdkvendors.VideoApplication
import com.himanshu.videocallsdkvendors.di.twilio.modules.RoomManagerModule
import com.himanshu.videocallsdkvendors.di.twilio.modules.TwilioVideoCallModule
import com.himanshu.videocallsdkvendors.di.twilio.modules.VideoServiceModule
import dagger.Component
import dagger.android.AndroidInjectionModule

@ApplicationScope
@Component(modules = [
    AndroidInjectionModule::class,
    ApplicationModule::class,
    RoomManagerModule::class,
    TwilioVideoCallModule::class,
    VideoServiceModule::class])
interface VideoApplicationComponent {
    fun inject(application: VideoApplication?)
}