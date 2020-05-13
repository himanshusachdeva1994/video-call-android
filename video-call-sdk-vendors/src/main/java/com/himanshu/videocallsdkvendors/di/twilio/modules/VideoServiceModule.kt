package com.himanshu.videocallsdkvendors.di.twilio.modules

import com.himanshu.videocallsdkvendors.di.twilio.subcomponents.VideoServiceSubcomponent
import com.himanshu.videocallsdkvendors.services.twilio.TwilioVideoService
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [VideoServiceSubcomponent::class])
abstract class VideoServiceModule {
    @Binds
    @IntoMap
    @ClassKey(TwilioVideoService::class)
    abstract fun bindYourServiceInjectorFactory(factory: VideoServiceSubcomponent.Factory?): AndroidInjector.Factory<*>?
}