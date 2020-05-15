package com.himanshu.videocallsdkvendors.broadcast;

import android.content.Context;

import timber.log.Timber;

/**
 * @author : Himanshu Sachdeva
 * @created : 14-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
public class CallReceiver extends PhonecallReceiver {

    @Override
    public void onIncomingCallStarted(Context context) {
        Timber.d("onIncomingCallStarted");
    }

    @Override
    public void onOutgoingCallStarted(Context context) {
        Timber.d("onOutgoingCallStarted");
    }

    @Override
    public void onIncomingCallEnded(Context context) {
        Timber.d("onIncomingCallEnded");
    }

    @Override
    public void onOutgoingCallEnded(Context context) {
        Timber.d("onOutgoingCallEnded");
    }

    @Override
    public void onMissedCall(Context context) {
        Timber.d("onMissedCall");
    }

    @Override
    public void onIncomingCallAnswered(Context context) {
        Timber.d("onIncomingCallAnswered");
    }
}