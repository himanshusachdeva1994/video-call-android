package com.himanshu.videocallsdkvendors.helper;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.himanshu.videocallsdkvendors.constants.VideoSdkVendors;
import com.himanshu.videocallsdkvendors.view.activity.TwilioVideoCallActivity;

import static com.himanshu.videocallsdkvendors.constants.IntentKeyConstants.AUTH_TOKEN;

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
public class VideoCallSdkHelper {

    private static VideoCallSdkHelper instance;

    private Activity activity;
    private VideoSdkVendors sdkVendor;
    private String authToken;

    private VideoCallSdkHelper(Activity activity) {
        this.activity = activity;
    }

    public static VideoCallSdkHelper getInstance(Activity activity) {
        if (instance == null) {
            instance = new VideoCallSdkHelper(activity);
        }
        return instance;
    }

    public VideoCallSdkHelper setVendor(VideoSdkVendors sdkVendor) {
        this.sdkVendor = sdkVendor;
        return instance;
    }

    public VideoCallSdkHelper setAuthToken(String authToken) {
        this.authToken = authToken;
        return instance;
    }

    public void init() {
        if (sdkVendor == null) {
            throw new Error("SDK Vendor not specified");
        }

        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(activity, "Auth Token not specified", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sdkVendor == VideoSdkVendors.TWILIO) {
            Intent intent = new Intent(activity, TwilioVideoCallActivity.class);
            intent.putExtra(AUTH_TOKEN, authToken);
            activity.startActivity(intent);
        } else {
            throw new Error("SDK support not implemented");
        }
    }
}
