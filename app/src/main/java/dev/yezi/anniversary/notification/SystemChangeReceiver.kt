package dev.yezi.anniversary.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.yezi.anniversary.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SystemChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).eventDao()
                dao.getEnabledReminders().forEach { reminder ->
                    val event = dao.getEvent(reminder.eventId) ?: return@forEach
                    ReminderScheduler.schedule(context, event, reminder)
                }
                dev.yezi.anniversary.widget.SingleEventWidgetReceiver.refresh(context)
            } finally { pending.finish() }
        }
    }
}
