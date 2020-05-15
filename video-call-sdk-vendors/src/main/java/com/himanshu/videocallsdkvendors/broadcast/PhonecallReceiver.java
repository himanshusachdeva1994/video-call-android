package com.himanshu.videocallsdkvendors.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.himanshu.videocallsdkvendors.interfaces.PhoneCallStateListenerCallbacks;

/**
 * @author : Himanshu Sachdeva
 * @created : 14-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
public abstract class PhonecallReceiver extends BroadcastReceiver implements PhoneCallStateListenerCallbacks {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getExtras() != null && intent.hasExtra(TelephonyManager.EXTRA_STATE)) {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

                if (stateStr != null) {
                    int state = TelephonyManager.CALL_STATE_IDLE;
                    if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        state = TelephonyManager.CALL_STATE_OFFHOOK;
                    } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        state = TelephonyManager.CALL_STATE_RINGING;
                    }
                    onCallStateChanged(context, state);
                }
            }
        }
    }

    //Deals with actual events
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                onIncomingCallStarted(context);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = true;
                    onIncomingCallAnswered(context);
                } else {
                    isIncoming = false;
                    onOutgoingCallStarted(context);
                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context);
                } else if (isIncoming) {
                    onIncomingCallEnded(context);
                } else {
                    onOutgoingCallEnded(context);
                }
                break;
        }
        lastState = state;
    }
}