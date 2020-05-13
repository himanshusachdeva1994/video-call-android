package com.himanshu.videocallsdkvendors.view.activity.base

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.himanshu.videocallsdkvendors.R
import com.himanshu.videocallsdkvendors.interfaces.DataBindingViewClickCallbacks
import dagger.android.AndroidInjection

/**
 * @author : Himanshu Sachdeva
 * @created : 12-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
abstract class BaseActivity : AppCompatActivity(), DataBindingViewClickCallbacks {

    protected val context: Context = this
    val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState)
    }

    override fun onClickEvent(@Nullable view: View?) {}
}