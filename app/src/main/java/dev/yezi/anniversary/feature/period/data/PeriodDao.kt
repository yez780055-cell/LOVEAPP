package dev.yezi.anniversary.feature.period.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDao {
    @Query("SELECT * FROM period_records ORDER BY startDate")
    fun observeRecords(): Flow<List<PeriodRecordEntity>>
}
