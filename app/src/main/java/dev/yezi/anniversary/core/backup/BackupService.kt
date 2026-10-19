package dev.yezi.anniversary.core.backup

import android.net.Uri

enum class BackupScope { TODO, PERIOD, ANNIVERSARY, ALL }

data class BackupPreview(val scopes: Set<BackupScope>, val itemCount: Int, val formatVersion: Int)

interface BackupService {
    suspend fun inspect(source: Uri): BackupPreview
    suspend fun export(destination: Uri, scopes: Set<BackupScope>)
    suspend fun importAfterConfirmation(source: Uri, scopes: Set<BackupScope>)
}
