package dev.yezi.anniversary.feature.anniversary.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.yezi.anniversary.R
import dev.yezi.anniversary.app.MainActivity
import dev.yezi.anniversary.core.database.AppDatabase
import dev.yezi.anniversary.core.notification.ReminderScheduleSpec
import dev.yezi.anniversary.core.notification.ReminderScheduler
import dev.yezi.anniversary.feature.anniversary.data.AnniversaryReminderSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnniversaryReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AnniversaryReminderSchedule.DELIVERY_ACTION) return
        if (intent.getStringExtra(ReminderScheduler.EXTRA_SOURCE_TYPE) != AnniversaryReminderSchedule.SOURCE_TYPE) return
        val sourceId = intent.getStringExtra(ReminderScheduler.EXTRA_SOURCE_ID) ?: return
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.get(context)
                val event = database.eventDao().getEvent(sourceId) ?: return@launch
                val reminder = database.reminderDao().getById(reminderId) ?: return@launch
                if (!reminder.enabled ||
                    reminder.sourceType != AnniversaryReminderSchedule.SOURCE_TYPE ||
                    reminder.sourceId != event.id
                ) return@launch

                createChannel(context, reminder.notificationChannel)
                val notificationsAllowed = Build.VERSION.SDK_INT < 33 ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (notificationsAllowed) {
                    val open = Intent(context, MainActivity::class.java).putExtra("open_event_id", event.id)
                    val contentIntent = PendingIntent.getActivity(
                        context,
                        event.id.hashCode(),
                        open,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val notification = NotificationCompat.Builder(context, reminder.notificationChannel)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(event.title)
                        .setContentText("今天是重要的日子")
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .build()
                    NotificationManagerCompat.from(context).notify(event.id.hashCode(), notification)
                }

                val next = AnniversaryReminderSchedule.resolve(event, reminder)
                if (event.repeatType == "yearly" && next.triggerAtEpochMillis != null) {
                    database.reminderDao().upsert(next.reminder)
                    ReminderScheduler.schedule(context, next)
                } else {
                    ReminderScheduler.cancel(context, reminder)
                    database.reminderDao().upsert(reminder.copy(enabled = false, nextTriggerAt = null))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "纪念日提醒", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
