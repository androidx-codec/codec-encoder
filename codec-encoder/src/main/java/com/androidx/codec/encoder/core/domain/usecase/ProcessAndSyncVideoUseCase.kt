package com.androidx.codec.encoder.core.domain.usecase

import com.androidx.codec.encoder.core.domain.model.EncodeResult
import com.androidx.codec.encoder.core.domain.model.MediaFile
import com.androidx.codec.encoder.core.domain.model.SyncResult
import com.androidx.codec.encoder.core.domain.repository.MediaRepository

sealed class VideoProcessingResult {
    data class Success(val playableUri: String) : VideoProcessingResult()
    data class Error(val message: String) : VideoProcessingResult()
}

class ProcessAndSyncVideoUseCase(
    private val repository: MediaRepository
) {
    suspend fun compressVideo(mediaFile: MediaFile): EncodeResult {
        return repository.encodeVideo(mediaFile)
    }

    suspend fun uploadVideo(mediaFile: MediaFile, storageUrl: String): SyncResult {
        return repository.syncToStorage(mediaFile, storageUrl)
    }

    suspend fun processAndSync(mediaFile: MediaFile, storageUrl: String? = null): VideoProcessingResult {
        val encodeResult = repository.encodeVideo(mediaFile)
        if (!encodeResult.success) {
            return VideoProcessingResult.Error(encodeResult.errorMessage ?: "Video compression failed")
        }

        val compressedFile = mediaFile.copy(
            uri = encodeResult.outputUri,
            filePath = encodeResult.outputUri
        )

        val syncResult = repository.syncToStorage(compressedFile, storageUrl ?: "")
        return if (syncResult.success && syncResult.storageUrl.isNotBlank()) {
            VideoProcessingResult.Success(syncResult.storageUrl)
        } else {
            VideoProcessingResult.Success(compressedFile.uri)
        }
    }
}
