package com.himanshu.videocallsdkvendors.util;

import android.Manifest;

/**
 * @author : Himanshu Sachdeva
 * @created : 11-Mar-2021
 * @email : himanshu.sachdeva1994@gmail.com
 */
public class GlobalUtil {

    public static String[] getPermissionListForVideoCall() {
        return new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }
}
