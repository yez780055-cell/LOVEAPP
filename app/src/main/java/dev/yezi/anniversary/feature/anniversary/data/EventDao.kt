package dev.yezi.anniversary.feature.anniversary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.yezi.anniversary.core.database.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE trashedAt IS NULL AND archivedAt IS NULL ORDER BY isPinned DESC, sortOrder ASC, targetDate ASC")
    fun observeActiveEvents(): Flow<List<EventEntity>>
    @Query("SELECT * FROM events WHERE id = :id LIMIT 1") suspend fun getEvent(id: String): EventEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertEvent(event: EventEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertCategory(category: CategoryEntity)
    @Query("SELECT * FROM categories ORDER BY sortOrder, name") fun observeCategories(): Flow<List<CategoryEntity>>
    @Query("DELETE FROM events WHERE id = :id") suspend fun deleteEvent(id: String)
    @Query("SELECT * FROM reminders WHERE enabled = 1") suspend fun getEnabledReminders(): List<ReminderEntity>
    @Query("SELECT * FROM reminders WHERE eventId = :eventId") suspend fun getRemindersForEvent(eventId: String): List<ReminderEntity>
    @Query("SELECT EXISTS(SELECT 1 FROM reminders WHERE eventId = :eventId AND enabled = 1)") suspend fun hasEnabledReminder(eventId: String): Boolean
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertReminder(reminder: ReminderEntity)
    @Query("DELETE FROM reminders WHERE id = :id") suspend fun deleteReminder(id: String)
    @Query("DELETE FROM reminders WHERE eventId = :eventId") suspend fun deleteRemindersForEvent(eventId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertImage(image: BackgroundImageEntity)
    @Query("SELECT * FROM background_images WHERE id = :id LIMIT 1") suspend fun getImage(id: String): BackgroundImageEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertMilestone(milestone: MilestoneEntity)
    @Transaction suspend fun trashEvent(id: String) {
        val current = getEvent(id) ?: return
        upsertEvent(current.copy(trashedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
    }
}
