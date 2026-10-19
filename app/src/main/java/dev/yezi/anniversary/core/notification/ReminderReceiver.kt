package dev.yezi.anniversary.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.yezi.anniversary.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bridges alarms scheduled by the pre-v3 explicit receiver to the new source-based delivery action.
 * Remove after existing installations have had a release cycle to migrate their scheduled alarms.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val legacySourceId = intent.getStringExtra(EXTRA_LEGACY_EVENT_ID) ?: return
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = AppDatabase.get(context).reminderDao().getById(reminderId) ?: return@launch
                if (reminder.sourceId != legacySourceId || !reminder.enabled) return@launch

                val legacyPending = Intent(context, ReminderReceiver::class.java)
                val oldAlarm = android.app.PendingIntent.getBroadcast(
                    context,
                    reminder.requestCode,
                    legacyPending,
                    android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                oldAlarm?.let {
                    context.getSystemService(android.app.AlarmManager::class.java).cancel(it)
                    it.cancel()
                }

                val delivery = Intent(reminder.deliveryAction)
                    .setPackage(context.packageName)
                    .putExtra(ReminderScheduler.EXTRA_SOURCE_TYPE, reminder.sourceType)
                    .putExtra(ReminderScheduler.EXTRA_SOURCE_ID, reminder.sourceId)
                    .putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
                context.sendBroadcast(delivery)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val EXTRA_LEGACY_EVENT_ID = "event_id"
    }
}
