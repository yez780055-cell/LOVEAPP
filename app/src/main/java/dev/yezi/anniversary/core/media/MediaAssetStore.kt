package dev.yezi.anniversary.core.media

import android.net.Uri

enum class MediaOwnerType { ANNIVERSARY, TODO, PERIOD }

interface MediaAssetStore {
    suspend fun copyToPrivateStorage(source: Uri, ownerType: MediaOwnerType, ownerId: String): String
    suspend fun deleteIfUnreferenced(assetId: String)
}
