package com.standbyus.app.widget

import android.content.Context
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.ui.theme.StatusEmoji

class StandByWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val status = loadCachedStatus(context)
        val bgColor = try {
            ColorProvider(Color(android.graphics.Color.parseColor(status?.feelingColor ?: "#FFD93D")))
        } catch (e: Exception) {
            ColorProvider(Color(android.graphics.Color.parseColor("#FFD93D")))
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgColor),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = StatusEmoji.textFallback(status?.feelingEmoji, status?.feeling),
                    style = TextStyle(fontSize = 48.sp)
                )
                Text(
                    text = status?.feeling ?: "StandBy Us",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
                val doingText = status?.let { it.customDoing.ifEmpty { it.doing } } ?: ""
                if (doingText.isNotEmpty()) {
                    Text(
                        text = doingText,
                        style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color.DarkGray))
                    )
                }
                if (!status?.note.isNullOrEmpty()) {
                    Text(
                        text = status!!.note,
                        style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.Gray))
                    )
                }
            }
        }
    }

    private fun loadCachedStatus(context: Context): UserStatus? {
        val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
        return UserStatus(
            doing = prefs.getString("doing", "") ?: "",
            customDoing = prefs.getString("customDoing", "") ?: "",
            feeling = prefs.getString("feeling", "开心") ?: "开心",
            feelingColor = prefs.getString("feelingColor", "#FFD93D") ?: "#FFD93D",
            feelingEmoji = prefs.getString("feelingEmoji", "😊") ?: "😊",
            note = prefs.getString("note", "") ?: "",
            updatedAt = prefs.getLong("updatedAt", System.currentTimeMillis())
        )
    }
}
