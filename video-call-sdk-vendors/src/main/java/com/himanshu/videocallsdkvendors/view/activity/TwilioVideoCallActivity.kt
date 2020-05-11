package com.himanshu.videocallsdkvendors.view.activity

import android.os.Bundle
import android.view.Menu
import com.himanshu.videocallsdkvendors.R
import com.himanshu.videocallsdkvendors.view.activity.base.BaseActivity


/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
class TwilioVideoCallActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_twilio_video_call)
    }

    override fun onPermissionsGranted() {
        setUpLocalMedia()
    }

    private fun setUpLocalMedia() {
        TODO("Not yet implemented")
    }
}