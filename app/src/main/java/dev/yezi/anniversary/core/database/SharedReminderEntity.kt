package dev.yezi.anniversary.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reminders", indices = [Index(value = ["sourceType", "sourceId"])])
data class ReminderEntity(
    @PrimaryKey val id: String,
    val sourceType: String,
    val sourceId: String,
    val enabled: Boolean = true,
    val triggerType: String = "on_day",
    val offsetDays: Int = 0,
    val localTime: String = "09:00",
    val exact: Boolean = true,
    val nextTriggerAt: Long? = null,
    val notificationChannel: String = "life_space_reminders",
    val requestCode: Int,
    val deliveryAction: String
)
