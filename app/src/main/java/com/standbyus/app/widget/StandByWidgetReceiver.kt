package com.standbyus.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class StandByWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "StandByWidgetReceiver"
    }

    override val glanceAppWidget: GlanceAppWidget
        get() = StandByWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called: appWidgetIds=${appWidgetIds.contentToString()}")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: first widget placed")
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: last widget removed")
        super.onDisabled(context)
    }
}
