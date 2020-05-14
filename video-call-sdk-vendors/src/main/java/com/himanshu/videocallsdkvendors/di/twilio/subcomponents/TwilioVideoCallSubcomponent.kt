package com.himanshu.videocallsdkvendors.di.twilio.subcomponents

import com.himanshu.videocallsdkvendors.view.activity.TwilioVideoCallActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TwilioVideoCallSubcomponent : AndroidInjector<TwilioVideoCallActivity?> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<TwilioVideoCallActivity?>
}