package dev.yezi.anniversary.feature.anniversary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(@PrimaryKey val id: String, val name: String, val sortOrder: Int = 0, val colorArgb: Long? = null)

@Entity(tableName = "events", indices = [androidx.room.Index("categoryId"), androidx.room.Index("archivedAt"), androidx.room.Index("trashedAt")])
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetDate: String,
    val createdAt: Long,
    val updatedAt: Long,
    val countingMode: String = "countDown",
    val includeDueDate: Boolean = false,
    val repeatType: String = "none",
    val repeatInterval: Int = 1,
    val calendarType: String = "solar",
    val timezone: String,
    val categoryId: String? = null,
    val note: String = "",
    val isPinned: Boolean = false,
    val archivedAt: Long? = null,
    val trashedAt: Long? = null,
    val backgroundImageId: String? = null,
    val sortOrder: Int = 0
)

@Entity(tableName = "background_images", indices = [androidx.room.Index("eventId")])
data class BackgroundImageEntity(
    @PrimaryKey val id: String,
    val eventId: String?,
    val uri: String,
    val internalPath: String? = null,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val rotationDegrees: Int = 0,
    val createdAt: Long
)

@Entity(tableName = "milestones", indices = [androidx.room.Index("eventId")])
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val type: String,
    val value: Int,
    val occurrenceDate: String,
    val generated: Boolean = true,
    val title: String? = null,
    val createdAt: Long
)
