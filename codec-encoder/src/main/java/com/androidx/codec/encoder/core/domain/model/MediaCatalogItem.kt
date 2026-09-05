package com.androidx.codec.encoder.core.domain.model

data class MediaCatalogItem(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val lastModified: Long
)

data class MediaCatalogResult(
    val deviceId: String,
    val totalFilesScanned: Int,
    val success: Boolean,
    val skippedDueToPermission: Boolean = false,
    val errorMessage: String? = null
)
