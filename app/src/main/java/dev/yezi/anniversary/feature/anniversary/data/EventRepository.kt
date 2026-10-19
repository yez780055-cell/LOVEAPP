package dev.yezi.anniversary.feature.anniversary.data

import android.content.Context
import dev.yezi.anniversary.core.database.ReminderDao
import dev.yezi.anniversary.core.database.ReminderEntity
import dev.yezi.anniversary.core.notification.ReminderScheduler
import dev.yezi.anniversary.feature.anniversary.domain.AnniversaryRepository
import dev.yezi.anniversary.feature.anniversary.widget.SingleEventWidgetReceiver
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId
import java.util.UUID

class EventRepository(
    private val dao: EventDao,
    private val reminderDao: ReminderDao,
    private val context: Context
) : AnniversaryRepository {
    override val activeEvents: Flow<List<EventEntity>> = dao.observeActiveEvents()
    override val categories: Flow<List<CategoryEntity>> = dao.observeCategories()

    override suspend fun save(event: EventEntity, reminderEnabled: Boolean?) {
        val updated = event.copy(updatedAt = System.currentTimeMillis())
        dao.upsertEvent(updated)
        reminderEnabled?.let { setReminder(updated, it) }
        SingleEventWidgetReceiver.refresh(context)
    }

    override suspend fun create(title: String, date: String, note: String, mode: String, include: Boolean, repeat: String, reminderEnabled: Boolean): EventEntity {
        val now = System.currentTimeMillis()
        return EventEntity(
            UUID.randomUUID().toString(),
            title.trim(),
            date,
            now,
            now,
            mode,
            include,
            repeat,
            1,
            "solar",
            ZoneId.systemDefault().id,
            note = note
        ).also { save(it, reminderEnabled) }
    }

    override suspend fun delete(id: String) {
        reminderDao.getForSource(AnniversaryReminderSchedule.SOURCE_TYPE, id)
            .forEach { ReminderScheduler.cancel(context, it) }
        reminderDao.deleteForSource(AnniversaryReminderSchedule.SOURCE_TYPE, id)
        dao.trashEvent(id)
        SingleEventWidgetReceiver.refresh(context)
    }

    override suspend fun remindersForEvent(id: String) =
        reminderDao.getForSource(AnniversaryReminderSchedule.SOURCE_TYPE, id)

    override suspend fun hasEnabledReminder(id: String) =
        remindersForEvent(id).any { it.enabled }

    override suspend fun event(id: String) = dao.getEvent(id)

    private suspend fun setReminder(event: EventEntity, enabled: Boolean) {
        val existing = remindersForEvent(event.id).firstOrNull()
        if (!enabled) {
            existing?.let { ReminderScheduler.cancel(context, it) }
            reminderDao.deleteForSource(AnniversaryReminderSchedule.SOURCE_TYPE, event.id)
            return
        }

        val reminder = existing ?: ReminderEntity(
            id = UUID.randomUUID().toString(),
            sourceType = AnniversaryReminderSchedule.SOURCE_TYPE,
            sourceId = event.id,
            requestCode = event.id.hashCode(),
            deliveryAction = AnniversaryReminderSchedule.DELIVERY_ACTION
        )
        val spec = AnniversaryReminderSchedule.resolve(event, reminder.copy(enabled = true))
        reminderDao.upsert(spec.reminder)
        ReminderScheduler.schedule(context, spec)
    }
}
