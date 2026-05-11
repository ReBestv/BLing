package com.standbyus.app

import android.app.Application
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class StandByApplication : Application() {

    @Inject
    lateinit var supabaseService: SupabaseService

    override fun onCreate() {
        super.onCreate()
        supabaseService.getDeviceId(this)
    }
}
