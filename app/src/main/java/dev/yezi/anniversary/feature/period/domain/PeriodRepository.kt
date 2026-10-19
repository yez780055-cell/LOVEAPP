package dev.yezi.anniversary.feature.period.domain

import dev.yezi.anniversary.feature.period.data.PeriodRecordEntity
import kotlinx.coroutines.flow.Flow

interface PeriodRepository {
    fun observeRecords(): Flow<List<PeriodRecordEntity>>
}
