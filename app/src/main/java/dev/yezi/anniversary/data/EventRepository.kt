package dev.yezi.anniversary.data

import kotlinx.coroutines.flow.Flow
import java.time.ZoneId
import java.util.UUID
import dev.yezi.anniversary.notification.ReminderScheduler
import android.content.Context
import dev.yezi.anniversary.widget.SingleEventWidgetReceiver

class EventRepository(private val dao: EventDao, private val context: Context) {
    val activeEvents: Flow<List<EventEntity>> = dao.observeActiveEvents()
    val categories: Flow<List<CategoryEntity>> = dao.observeCategories()
    suspend fun save(event: EventEntity, reminderEnabled: Boolean?) {
        val updated = event.copy(updatedAt = System.currentTimeMillis())
        dao.upsertEvent(updated)
        reminderEnabled?.let { setReminder(updated, it) }
        SingleEventWidgetReceiver.refresh(context)
    }
    suspend fun create(title: String, date: String, note: String, mode: String, include: Boolean, repeat: String, reminderEnabled: Boolean): EventEntity {
        val now = System.currentTimeMillis()
        return EventEntity(UUID.randomUUID().toString(), title.trim(), date, now, now, mode, include, repeat, 1, "solar", ZoneId.systemDefault().id, note = note).also { save(it, reminderEnabled) }
    }
    suspend fun delete(id: String) {
        dao.getRemindersForEvent(id).forEach { ReminderScheduler.cancel(context, it) }
        dao.deleteRemindersForEvent(id)
        dao.trashEvent(id)
        SingleEventWidgetReceiver.refresh(context)
    }
    suspend fun remindersForEvent(id: String) = dao.getRemindersForEvent(id)
    suspend fun hasEnabledReminder(id: String) = dao.hasEnabledReminder(id)
    suspend fun event(id: String) = dao.getEvent(id)
    private suspend fun setReminder(event: EventEntity, enabled: Boolean) {
        val existing = dao.getRemindersForEvent(event.id).firstOrNull()
        if (!enabled) {
            existing?.let { ReminderScheduler.cancel(context, it); dao.deleteReminder(it.id) }
            return
        }
        val reminder = existing ?: ReminderEntity(UUID.randomUUID().toString(), event.id, requestCode = event.id.hashCode())
        val active = reminder.copy(enabled = true)
        val next = ReminderScheduler.schedule(context, event, active)
        dao.upsertReminder(active.copy(nextTriggerAt = next))
    }
}
