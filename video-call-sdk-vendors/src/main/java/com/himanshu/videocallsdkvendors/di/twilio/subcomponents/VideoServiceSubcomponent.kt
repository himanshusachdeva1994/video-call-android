package com.himanshu.videocallsdkvendors.di.twilio.subcomponents

import com.himanshu.videocallsdkvendors.services.twilio.TwilioVideoService
import dagger.Subcomponent
import dagger.android.AndroidInjector

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
@Subcomponent
interface VideoServiceSubcomponent : AndroidInjector<TwilioVideoService?> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<TwilioVideoService?>
}