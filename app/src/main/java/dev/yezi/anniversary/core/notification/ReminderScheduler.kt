package dev.yezi.anniversary.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.yezi.anniversary.core.database.ReminderEntity

object ReminderScheduler {
    const val EXTRA_SOURCE_TYPE = "source_type"
    const val EXTRA_SOURCE_ID = "source_id"
    const val EXTRA_REMINDER_ID = "reminder_id"

    fun schedule(context: Context, spec: ReminderScheduleSpec): Long? {
        val reminder = spec.reminder
        val triggerAt = spec.triggerAtEpochMillis
        if (!reminder.enabled || triggerAt == null || triggerAt <= System.currentTimeMillis()) {
            cancel(context, reminder)
            return null
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(context, reminder)
        runCatching {
            if (reminder.exact && Build.VERSION.SDK_INT >= 23 &&
                (Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms())
            ) {
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
        val pending = pendingIntent(context, reminder)
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(context: Context, reminder: ReminderEntity): PendingIntent {
        val intent = Intent(reminder.deliveryAction)
            .setPackage(context.packageName)
            .putExtra(EXTRA_SOURCE_TYPE, reminder.sourceType)
            .putExtra(EXTRA_SOURCE_ID, reminder.sourceId)
            .putExtra(EXTRA_REMINDER_ID, reminder.id)
        return PendingIntent.getBroadcast(
            context,
            reminder.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
