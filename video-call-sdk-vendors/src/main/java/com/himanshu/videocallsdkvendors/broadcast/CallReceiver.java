package com.himanshu.videocallsdkvendors.broadcast;

import android.content.Context;

import java.util.Date;

import timber.log.Timber;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Timber.d("onIncomingCallStarted %s", number);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Timber.d("onOutgoingCallStarted %s", number);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Timber.d("onIncomingCallEnded %s", number);
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Timber.d("onOutgoingCallEnded %s", number);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Timber.d("onMissedCall %s", number);
    }
}