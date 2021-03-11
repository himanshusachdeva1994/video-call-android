package com.himanshu.videocallsdkvendors.di.twilio

import android.app.Application
import dagger.Module
import dagger.Provides

/**
 * @author : Himanshu Sachdeva
 * @created : 13-May-2020
 * @email : himanshu.sachdeva1994@gmail.com
 */
@Module
class ApplicationModule(private val app: Application) {
    @Provides
    @ApplicationScope
    fun provideApplication(): Application {
        return app
    }
}