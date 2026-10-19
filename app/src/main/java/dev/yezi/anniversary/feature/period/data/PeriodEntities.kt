package dev.yezi.anniversary.feature.period.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "period_records")
data class PeriodRecordEntity(@PrimaryKey val id: String, val startDate: String, val endDate: String? = null)

@Entity(tableName = "period_settings")
data class PeriodSettingsEntity(@PrimaryKey val id: String = "default", val cycleLength: Int = 28, val periodLength: Int = 5)
