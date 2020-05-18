package com.himanshu.videocallsdkvendors.broadcast;

import android.content.Context;
import android.content.Intent;

import com.himanshu.videocallsdkvendors.constants.LocalBroadcastKeyConstants;
import com.himanshu.videocallsdkvendors.constants.PhoneCallStateConstants;

import timber.log.Timber;

import static com.himanshu.videocallsdkvendors.constants.IntentKeyConstants.PHONE_CALL_STATE;

/**
 * @author : Himanshu Sachdeva
 * @created : 14-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
public class CallReceiver extends PhoneCallReceiver {

    @Override
    public void onIncomingCallStarted(Context context) {
        Timber.d("onIncomingCallStarted");
    }

    @Override
    public void onOutgoingCallStarted(Context context) {
        Timber.d("onOutgoingCallStarted");

        Intent intent = new Intent();
        intent.setAction(LocalBroadcastKeyConstants.BROADCAST_PHONE_CALL_STATE);
        intent.putExtra(PHONE_CALL_STATE, PhoneCallStateConstants.OUTGOING_CALL_STARTED);
        context.sendBroadcast(intent);
    }

    @Override
    public void onIncomingCallEnded(Context context) {
        Timber.d("onIncomingCallEnded");

        Intent intent = new Intent();
        intent.setAction(LocalBroadcastKeyConstants.BROADCAST_PHONE_CALL_STATE);
        intent.putExtra(PHONE_CALL_STATE, PhoneCallStateConstants.INCOMING_CALL_ENDED);
        context.sendBroadcast(intent);
    }

    @Override
    public void onOutgoingCallEnded(Context context) {
        Timber.d("onOutgoingCallEnded");

        Intent intent = new Intent();
        intent.setAction(LocalBroadcastKeyConstants.BROADCAST_PHONE_CALL_STATE);
        intent.putExtra(PHONE_CALL_STATE, PhoneCallStateConstants.OUTGOING_CALL_ENDED);
        context.sendBroadcast(intent);
    }

    @Override
    public void onMissedCall(Context context) {
        Timber.d("onMissedCall");
    }

    @Override
    public void onIncomingCallAnswered(Context context) {
        Timber.d("onIncomingCallAnswered");

        Intent intent = new Intent();
        intent.setAction(LocalBroadcastKeyConstants.BROADCAST_PHONE_CALL_STATE);
        intent.putExtra(PHONE_CALL_STATE, PhoneCallStateConstants.INCOMING_CALL_ANSWERED);
        context.sendBroadcast(intent);
    }
}