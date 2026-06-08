package com.standbyus.app.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.standbyus.app.MainActivity
import com.standbyus.app.R
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.InteractionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartnerEventNotifier @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gate = PartnerEventNotificationGate(
        initialLastNotifiedKey = prefs.getLong(KEY_LAST_NOTIFIED, 0L).takeIf { it > 0L }
    )
    private var started = false

    fun start() {
        if (started) return
        started = true
        createNotificationChannel()

        val myUserId = supabaseService.getCachedDeviceId()
        if (myUserId.isEmpty()) {
            Log.w(TAG, "skip partner event notifier: no device id")
            return
        }

        scope.launch {
            interactionRepository.observeLatestReceivedInteraction(myUserId)
                .collect { interaction ->
                    if (hasNotificationPermission() && gate.shouldNotify(interaction)) {
                        showPoopCheckinNotification()
                        gate.lastNotifiedKey?.let {
                            prefs.edit().putLong(KEY_LAST_NOTIFIED, it).apply()
                        }
                    }
                }
        }
    }

    private fun showPoopCheckinNotification() {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Bling")
            .setContentText("拉屎大王发力中，噗噗噗^(*￣(oo)￣)^~~~~~")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(notificationSound)
            .setVibrate(VIBRATION_PATTERN)
            .setFullScreenIntent(pendingIntent, false)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "notification permission missing", e)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "伴侣动态",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "来自对方的打卡和互动提醒"
            setSound(notificationSound, audioAttributes)
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "PartnerEventNotifier"
        private const val PREFS_NAME = "partner_event_notifications"
        private const val KEY_LAST_NOTIFIED = "last_notified_key"
        private const val CHANNEL_ID = "partner_events_heads_up_v2"
        private const val NOTIFICATION_ID = 20260521
        private val VIBRATION_PATTERN = longArrayOf(0L, 250L, 120L, 250L)
    }
}
