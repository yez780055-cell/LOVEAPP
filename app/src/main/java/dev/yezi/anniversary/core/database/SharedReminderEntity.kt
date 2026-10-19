package dev.yezi.anniversary.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import dev.yezi.anniversary.feature.period.data.PeriodRecordEntity
import dev.yezi.anniversary.feature.todo.data.TodoTaskEntity

@Entity(tableName = "reminders", indices = [Index("eventId"), Index(value = ["sourceType", "sourceId"])])
data class ReminderEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val enabled: Boolean = true,
    val triggerType: String = "on_day",
    val offsetDays: Int = 0,
    val localTime: String = "09:00",
    val exact: Boolean = true,
    val nextTriggerAt: Long? = null,
    val notificationChannel: String = "anniversary_reminders",
    val requestCode: Int,
    val sourceType: String = "anniversary",
    val sourceId: String = eventId
)

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_tasks ORDER BY dueDate, title")
    fun observeTasks(): Flow<List<TodoTaskEntity>>
}

@Dao
interface PeriodDao {
    @Query("SELECT * FROM period_records ORDER BY startDate")
    fun observeRecords(): Flow<List<PeriodRecordEntity>>
}
