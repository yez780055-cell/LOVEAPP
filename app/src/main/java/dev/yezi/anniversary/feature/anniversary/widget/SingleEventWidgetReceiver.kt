package dev.yezi.anniversary.feature.anniversary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.yezi.anniversary.R
import dev.yezi.anniversary.app.MainActivity
import dev.yezi.anniversary.core.database.AppDatabase
import dev.yezi.anniversary.core.date.CountingMode
import dev.yezi.anniversary.core.date.DateEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class SingleEventWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = update(context, manager, ids)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in SYSTEM_REFRESH_ACTIONS) {
            refresh(context)
            return
        }
        super.onReceive(context, intent)
    }

    companion object {
        private val SYSTEM_REFRESH_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED
        )

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SingleEventWidgetReceiver::class.java))
            if (ids.isNotEmpty()) update(context, manager, ids)
        }

        private fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            CoroutineScope(Dispatchers.IO).launch {
                val events = AppDatabase.get(context).eventDao().observeActiveEvents()
                events.first().firstOrNull()?.let { item ->
                    val zone = runCatching { java.time.ZoneId.of(item.timezone) }.getOrDefault(java.time.ZoneId.systemDefault())
                    val today = LocalDate.now(zone)
                    val anchor = LocalDate.parse(item.targetDate)
                    val target = if (item.repeatType == "yearly") {
                        DateEngine.nextOccurrence(anchor, today, dev.yezi.anniversary.core.date.RepeatType.YEARLY) ?: anchor
                    } else anchor
                    val mode = if (item.countingMode == "countUp") CountingMode.COUNT_UP else CountingMode.COUNT_DOWN
                    val days = DateEngine.dayCount(target, today, mode, item.includeDueDate).displayDays
                    ids.forEach { id ->
                        val views = RemoteViews(context.packageName, R.layout.widget_single_event)
                        views.setTextViewText(R.id.widget_title, item.title)
                        views.setTextViewText(R.id.widget_days, days.toString() + " 天")
                        val open = Intent(context, MainActivity::class.java).putExtra("open_event_id", item.id)
                        val click = PendingIntent.getActivity(context, id, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        views.setOnClickPendingIntent(R.id.widget_root, click)
                        manager.updateAppWidget(id, views)
                    }
                } ?: ids.forEach { id ->
                    val views = RemoteViews(context.packageName, R.layout.widget_single_event)
                    views.setTextViewText(R.id.widget_title, "记录一个纪念日")
                    views.setTextViewText(R.id.widget_days, "添加事件后显示天数")
                    manager.updateAppWidget(id, views)
                }
            }
        }
    }
}
