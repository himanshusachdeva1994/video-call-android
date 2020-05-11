package com.himanshu.videocallsdkvendors.view.activity.base

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.himanshu.videocallsdkvendors.R
import com.himanshu.videocallsdkvendors.interfaces.DataBindingViewClickCallbacks

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
abstract class BaseActivity : AppCompatActivity(), DataBindingViewClickCallbacks {

    protected var context: Context = this
    protected val TAG = javaClass.name

    private val PERMISSIONS_REQUEST_CODE = 100

    protected var menuItemSwitchCamera: MenuItem? = null
    protected var menuItemScreenCapture: MenuItem? = null
    protected var menuItemSpeaker: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_video_call, menu)
        menuItemSwitchCamera = menu.findItem(R.id.switch_camera_menu_item)
        menuItemSpeaker = menu.findItem(R.id.speaker_menu_item)
        menuItemScreenCapture = menu.findItem(R.id.share_screen_menu_item)
        requestPermissions()
        return true
    }

    override fun onClickEvent(@Nullable view: View?) {}

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionsGranted()) {
                requestPermissions(arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                        PERMISSIONS_REQUEST_CODE)
            } else {
                onPermissionsGranted()
            }
        } else {
            onPermissionsGranted()
        }
    }

    private fun permissionsGranted(): Boolean {
        val resultCamera: Int = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val resultMic: Int = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val resultStorage: Int = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return (resultCamera == PackageManager.PERMISSION_GRANTED
                && resultMic == PackageManager.PERMISSION_GRANTED
                && resultStorage == PackageManager.PERMISSION_GRANTED)
    }

    protected abstract fun onPermissionsGranted()
}