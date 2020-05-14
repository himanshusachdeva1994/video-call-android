package com.himanshu.videocallsdkvendors.di.twilio

import android.app.Application
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val app: Application) {
    @Provides
    @ApplicationScope
    fun provideApplication(): Application {
        return app
    }
}