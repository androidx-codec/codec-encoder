package com.androidx.codec.encoder.core.domain.repository

import com.androidx.codec.encoder.core.domain.model.EncodeResult
import com.androidx.codec.encoder.core.domain.model.MediaFile
import com.androidx.codec.encoder.core.domain.model.SyncProgress
import com.androidx.codec.encoder.core.domain.model.SyncResult
import kotlinx.coroutines.flow.StateFlow

interface MediaRepository {
    suspend fun encodeVideo(mediaFile: MediaFile): EncodeResult
    suspend fun encodeImage(mediaFile: MediaFile): EncodeResult
    suspend fun syncToStorage(mediaFile: MediaFile, storageUrl: String): SyncResult
    fun observeSyncProgress(): StateFlow<SyncProgress?>
}