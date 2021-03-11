package com.himanshu.videocallsdkvendors.di.twilio.modules

import com.himanshu.videocallsdkvendors.di.twilio.subcomponents.TwilioVideoCallSubcomponent
import com.himanshu.videocallsdkvendors.view.activity.TwilioVideoCallActivity
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
@Module(subcomponents = [TwilioVideoCallSubcomponent::class])
abstract class
TwilioVideoCallModule {
    @Binds
    @IntoMap
    @ClassKey(TwilioVideoCallActivity::class)
    abstract fun bindYourActivityInjectorFactory(builder: TwilioVideoCallSubcomponent.Factory?): AndroidInjector.Factory<*>?
}