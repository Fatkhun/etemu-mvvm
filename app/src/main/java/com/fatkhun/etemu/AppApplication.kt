package com.fatkhun.etemu

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.fatkhun.core.di.appModule
import com.fatkhun.core.di.repoModule
import com.fatkhun.core.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppApplication: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        startKoin {
            androidContext(this@AppApplication)
            modules(
                listOf(
                    appModule, repoModule, viewModelModule
                )
            )
        }
    }
}