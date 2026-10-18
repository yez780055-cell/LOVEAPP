package dev.yezi.anniversary.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.yezi.anniversary.data.EventEntity
import dev.yezi.anniversary.data.ReminderEntity
import dev.yezi.anniversary.domain.DateEngine
import dev.yezi.anniversary.domain.RepeatType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {
    const val EXTRA_EVENT_ID = "event_id"
    const val EXTRA_REMINDER_ID = "reminder_id"

    fun schedule(context: Context, event: EventEntity, reminder: ReminderEntity): Long? {
        if (!reminder.enabled) { cancel(context, reminder); return null }
        val zone = runCatching { ZoneId.of(event.timezone) }.getOrDefault(ZoneId.systemDefault())
        val anchor = runCatching { LocalDate.parse(event.targetDate) }.getOrNull() ?: return null
        val today = LocalDate.now(zone)
        val type = when (event.repeatType) { "yearly" -> RepeatType.YEARLY; else -> RepeatType.NONE }
        var occurrence = DateEngine.nextOccurrence(anchor, today, type) ?: return null
        val triggerDate = occurrence.minusDays(reminder.offsetDays.toLong())
        val localTime = runCatching { LocalTime.parse(reminder.localTime) }.getOrDefault(LocalTime.of(9, 0))
        var triggerAt = triggerDate.atTime(localTime).atZone(zone).toInstant().toEpochMilli()
        if (triggerAt <= System.currentTimeMillis()) {
            if (event.repeatType != "yearly") return null
            occurrence = DateEngine.nextOccurrence(anchor, today.plusDays(1), type) ?: return null
            triggerAt = occurrence.minusDays(reminder.offsetDays.toLong()).atTime(localTime).atZone(zone).toInstant().toEpochMilli()
        }
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(context, reminder, event.id)
        runCatching {
            if (reminder.exact && Build.VERSION.SDK_INT >= 23 && (Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms())) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        }.onFailure {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
        return triggerAt
    }

    fun cancel(context: Context, reminder: ReminderEntity) {
        val pending = pendingIntent(context, reminder, reminder.eventId)
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(context: Context, reminder: ReminderEntity, eventId: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        return PendingIntent.getBroadcast(context, reminder.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
