package dev.yezi.anniversary.core.notification

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
import dev.yezi.anniversary.app.MainActivity
import dev.yezi.anniversary.R
import dev.yezi.anniversary.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra(ReminderScheduler.EXTRA_EVENT_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).eventDao()
                val event = dao.getEvent(eventId) ?: return@launch
                val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID)
                val reminder = dao.getRemindersForEvent(eventId).firstOrNull { it.id == reminderId } ?: return@launch
                createChannel(context)
                val notificationsAllowed = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (notificationsAllowed) {
                    val open = Intent(context, MainActivity::class.java).putExtra("open_event_id", event.id)
                    val contentIntent = PendingIntent.getActivity(context, event.id.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val notification = NotificationCompat.Builder(context, "anniversary_reminders")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(event.title)
                        .setContentText("今天是重要的日子")
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .build()
                    NotificationManagerCompat.from(context).notify(event.id.hashCode(), notification)
                }
                if (event.repeatType == "yearly") {
                    val tomorrow = java.time.LocalDate.now(java.time.ZoneId.of(event.timezone)).plusDays(1)
                    val next = dev.yezi.anniversary.core.date.DateEngine.nextOccurrence(java.time.LocalDate.parse(event.targetDate), tomorrow, dev.yezi.anniversary.core.date.RepeatType.YEARLY)
                    val nextAt = next?.minusDays(reminder.offsetDays.toLong())?.atTime(java.time.LocalTime.parse(reminder.localTime))?.atZone(java.time.ZoneId.of(event.timezone))?.toInstant()?.toEpochMilli()
                    dao.upsertReminder(reminder.copy(nextTriggerAt = nextAt))
                    nextAt?.let { ReminderScheduler.schedule(context, event, reminder.copy(nextTriggerAt = it)) }
                } else {
                    ReminderScheduler.cancel(context, reminder)
                    dao.upsertReminder(reminder.copy(enabled = false, nextTriggerAt = null))
                }
            } finally { pendingResult.finish() }
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("anniversary_reminders", "纪念日提醒", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
