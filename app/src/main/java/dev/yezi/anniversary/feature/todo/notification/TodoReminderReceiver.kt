package dev.yezi.anniversary.feature.todo.notification

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
import dev.yezi.anniversary.core.database.AppDatabase
import dev.yezi.anniversary.core.notification.ReminderScheduler
import dev.yezi.anniversary.feature.todo.data.TodoReminderSchedule
import dev.yezi.anniversary.core.notification.ReminderScheduleSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TodoReminderSchedule.DELIVERY_ACTION || intent.getStringExtra(ReminderScheduler.EXTRA_SOURCE_TYPE) != TodoReminderSchedule.SOURCE_TYPE) return
        val taskId = intent.getStringExtra(ReminderScheduler.EXTRA_SOURCE_ID) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.get(context)
                val task = database.todoDao().task(taskId) ?: return@launch
                if (!task.reminderEnabled || task.isComplete) return@launch
                if (intent.getBooleanExtra(ReminderScheduler.EXTRA_RESCHEDULE, false)) {
                    val oldReminder = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID)?.let { database.reminderDao().getById(it) } ?: return@launch
                    val resolved = TodoReminderSchedule.resolve(task)
                    database.reminderDao().upsert(resolved.reminder)
                    ReminderScheduler.schedule(context, resolved)
                    return@launch
                }
                if (Build.VERSION.SDK_INT >= 26) context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("todo_reminders", "待办提醒", NotificationManager.IMPORTANCE_DEFAULT))
                if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    val open = PendingIntent.getActivity(context, taskId.hashCode(), Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val notice = NotificationCompat.Builder(context, "todo_reminders").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(task.title).setContentText("待办提醒").setContentIntent(open).setAutoCancel(true).build()
                    NotificationManagerCompat.from(context).notify(taskId.hashCode(), notice)
                }
                database.reminderDao().deleteForSource(TodoReminderSchedule.SOURCE_TYPE, taskId)
            } finally { pending.finish() }
        }
    }
}
