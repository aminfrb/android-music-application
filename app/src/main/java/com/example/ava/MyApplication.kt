package com.example.ava

import android.app.Application
import com.example.ava.di.appModule
import com.example.ava.di.databaseModule
import com.example.ava.di.networkModule
import com.example.ava.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(
                appModule,
                databaseModule,
                networkModule,
                repositoryModule
            )
        }
    }
}
