package com.himanshu.videocallsdkvendors

import android.app.Application
import com.himanshu.videocallsdkvendors.di.twilio.ApplicationModule
import com.himanshu.videocallsdkvendors.di.twilio.DaggerVideoApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva@instantsys.com
 */
class VideoApplication : Application(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        DaggerVideoApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .build()
                .inject(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }
}