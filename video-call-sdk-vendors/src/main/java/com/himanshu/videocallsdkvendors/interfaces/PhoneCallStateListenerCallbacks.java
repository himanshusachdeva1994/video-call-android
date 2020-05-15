package com.himanshu.videocallsdkvendors.interfaces;

import android.content.Context;

/**
 * @author : Himanshu Sachdeva
 * @created : 15-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
public interface PhoneCallStateListenerCallbacks {

    void onIncomingCallStarted(Context context);

    void onOutgoingCallStarted(Context context);

    void onIncomingCallEnded(Context context);

    void onOutgoingCallEnded(Context context);

    void onMissedCall(Context context);

    void onIncomingCallAnswered(Context context);
}
