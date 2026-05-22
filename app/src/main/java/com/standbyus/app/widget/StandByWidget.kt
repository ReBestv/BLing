package com.standbyus.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.standbyus.app.MainActivity
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.ui.theme.StatusEmoji

class StandByWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val status = loadCachedStatus(context)
        val bgColor = ColorProvider(
            if (status == null) {
                StandByWidgetStyle.emptyBackgroundColor
            } else {
                StandByWidgetStyle.backgroundColorFor(status.feelingKey)
            }
        )
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(bgColor)
                    .clickable(actionStartActivity(openAppIntent))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                if (status == null) {
                    EmptyWidgetContent()
                } else {
                    Text(
                        text = StatusEmoji.textFallback(
                            status.feelingAsset,
                            status.feelingKey,
                            status.feelingFallbackEmoji
                        ),
                        style = TextStyle(fontSize = 46.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = status.feelingLabel.ifEmpty { "对方状态" },
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(StandByWidgetStyle.primaryTextColor)
                        )
                    )

                    val doingText = status.stickerLabel.orEmpty()
                        .ifEmpty { status.customDoing.ifEmpty { status.doing } }
                    if (doingText.isNotEmpty()) {
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = doingText,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ColorProvider(StandByWidgetStyle.secondaryTextColor)
                            )
                        )
                    }

                    if (status.note.isNotEmpty()) {
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = status.note,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = ColorProvider(StandByWidgetStyle.tertiaryTextColor)
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun EmptyWidgetContent() {
        Text(
            text = "💗",
            style = TextStyle(fontSize = 42.sp)
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = "等待对方状态",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(StandByWidgetStyle.primaryTextColor)
            )
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = "点按打开 StandBy Us",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(StandByWidgetStyle.secondaryTextColor)
            )
        )
    }

    private fun loadCachedStatus(context: Context): UserStatus? {
        val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
        if (!prefs.contains("updatedAt")) return null

        return UserStatus(
            doing = prefs.getString("doing", "") ?: "",
            customDoing = prefs.getString("customDoing", "") ?: "",
            themeId = prefs.getString("themeId", "default") ?: "default",
            themeName = prefs.getString("themeName", "默认表情") ?: "默认表情",
            feelingKey = prefs.getString("feelingKey", "happy") ?: "happy",
            feelingLabel = prefs.getString("feelingLabel", "开心") ?: "开心",
            feelingAsset = prefs.getString("feelingAsset", "😊") ?: "😊",
            feelingFallbackEmoji = prefs.getString("feelingFallbackEmoji", "😊") ?: "😊",
            feelingColor = prefs.getString("feelingColor", "#FFFFD180") ?: "#FFFFD180",
            stickerId = prefs.getString("stickerId", null),
            stickerLabel = prefs.getString("stickerLabel", null),
            stickerAsset = prefs.getString("stickerAsset", null),
            note = prefs.getString("note", "") ?: "",
            updatedAt = prefs.getLong("updatedAt", System.currentTimeMillis())
        )
    }
}
