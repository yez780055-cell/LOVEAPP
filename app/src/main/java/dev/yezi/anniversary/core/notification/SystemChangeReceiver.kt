package dev.yezi.anniversary.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.yezi.anniversary.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SystemChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.get(context).reminderDao().getEnabledReminders().forEach { reminder ->
                    ReminderScheduler.requestReschedule(context, reminder)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
