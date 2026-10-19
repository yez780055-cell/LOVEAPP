package dev.yezi.anniversary.feature.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(tableName = "todo_tasks")
data class TodoTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dueDate: String? = null,
    val isComplete: Boolean = false,
    val scope: String = "personal",
    @ColumnInfo(defaultValue = "''") val note: String = "",
    val scheduledTime: String? = null,
    @ColumnInfo(defaultValue = "'[]'") val tagsJson: String = "[]",
    @ColumnInfo(defaultValue = "0") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val recurrenceJson: String? = null,
    @ColumnInfo(defaultValue = "'[]'") val recurrenceHistoryJson: String = "[]",
    val seriesId: String? = null,
    @ColumnInfo(defaultValue = "0") val reminderEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "10") val reminderMinutesBefore: Int = 10
)

@Entity(tableName = "todo_tags", indices = [Index(value = ["normalizedName"], unique = true)])
data class TodoTagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val normalizedName: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "todo_occurrences", primaryKeys = ["taskId", "periodDate"])
data class TodoOccurrenceEntity(
    val taskId: String,
    val periodDate: String,
    val scheduledDate: String,
    val completedAt: Long? = null,
    val titleOverride: String? = null,
    val noteOverride: String? = null,
    val timeOverride: String? = null,
    val tagsOverrideJson: String? = null,
    val reminderEnabledOverride: Boolean? = null,
    val reminderMinutesBeforeOverride: Int? = null,
    val skipped: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
