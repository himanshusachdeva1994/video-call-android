package com.himanshu.videocallsdkvendors.di.twilio.subcomponents;

import com.himanshu.videocallsdkvendors.view.activity.TwilioVideoCallActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface TwilioVideoCallSubcomponent extends AndroidInjector<TwilioVideoCallActivity> {
    @Subcomponent.Factory
    interface Factory extends AndroidInjector.Factory<TwilioVideoCallActivity> {
    }
}
