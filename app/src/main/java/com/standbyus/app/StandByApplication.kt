package com.standbyus.app

import android.app.Application
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.remote.ThemeRepository
import com.standbyus.app.notification.PartnerEventNotifier
import com.standbyus.app.ui.theme.EmojiThemeManager
import com.standbyus.app.widget.StandByWidget
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class StandByApplication : Application() {

    companion object {
        private const val TAG = "StandByApp"
    }

    @Inject
    lateinit var supabaseService: SupabaseService

    @Inject
    lateinit var themeRepository: ThemeRepository

    @Inject
    lateinit var partnerEventNotifier: PartnerEventNotifier

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        supabaseService.getDeviceId(this)
        partnerEventNotifier.start()

        appScope.launch {
            try {
                val manifest = themeRepository.fetchManifest(this@StandByApplication)
                if (manifest != null) {
                    EmojiThemeManager.updateThemes(manifest.themes)
                    Log.d(TAG, "theme manifest loaded: ${manifest.themes.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "theme manifest load failed", e)
            }
        }

        // Force widget update on app startup
        // Glance 1.1.x uses WorkManager; ensure widget renders
        appScope.launch {
            try {
                Log.d(TAG, "triggering initial widget updateAll")
                StandByWidget().updateAll(this@StandByApplication)
                Log.d(TAG, "initial widget updateAll completed")
            } catch (e: Exception) {
                Log.e(TAG, "initial widget updateAll failed", e)
            }
        }
    }
}
