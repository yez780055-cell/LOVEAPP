package dev.yezi.anniversary.feature.anniversary.domain

import dev.yezi.anniversary.core.database.ReminderEntity
import dev.yezi.anniversary.feature.anniversary.data.CategoryEntity
import dev.yezi.anniversary.feature.anniversary.data.EventEntity
import kotlinx.coroutines.flow.Flow

interface AnniversaryRepository {
    val activeEvents: Flow<List<EventEntity>>
    val categories: Flow<List<CategoryEntity>>
    suspend fun save(event: EventEntity, reminderEnabled: Boolean?)
    suspend fun create(title: String, date: String, note: String, mode: String, include: Boolean, repeat: String, reminderEnabled: Boolean): EventEntity
    suspend fun delete(id: String)
    suspend fun remindersForEvent(id: String): List<ReminderEntity>
    suspend fun hasEnabledReminder(id: String): Boolean
    suspend fun event(id: String): EventEntity?
}
