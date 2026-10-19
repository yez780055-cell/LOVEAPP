package dev.yezi.anniversary.core.settings

import kotlinx.coroutines.flow.Flow

data class AppSettings(val remindersEnabled: Boolean = true, val useSystemTheme: Boolean = true)

interface AppSettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(settings: AppSettings)
}
