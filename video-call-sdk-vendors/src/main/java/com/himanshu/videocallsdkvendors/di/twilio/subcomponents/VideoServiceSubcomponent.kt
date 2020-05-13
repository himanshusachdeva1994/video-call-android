package com.himanshu.videocallsdkvendors.di.twilio.subcomponents

import com.himanshu.videocallsdkvendors.services.twilio.TwilioVideoService
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface VideoServiceSubcomponent : AndroidInjector<TwilioVideoService?> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<TwilioVideoService?>
}