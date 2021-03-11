package com.himanshu.videocallsdkvendors.di.twilio.subcomponents

import com.himanshu.videocallsdkvendors.view.activity.TwilioVideoCallActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

/**
 * @author : Himanshu Sachdeva
 * @created : 14-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
@Subcomponent
interface TwilioVideoCallSubcomponent : AndroidInjector<TwilioVideoCallActivity?> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<TwilioVideoCallActivity?>
}