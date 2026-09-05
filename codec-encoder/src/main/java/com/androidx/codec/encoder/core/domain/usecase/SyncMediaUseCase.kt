package com.androidx.codec.encoder.core.domain.usecase

import com.androidx.codec.encoder.core.domain.model.MediaFile
import com.androidx.codec.encoder.core.domain.model.SyncResult
import com.androidx.codec.encoder.core.domain.repository.MediaRepository

class SyncMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(mediaFile: MediaFile, storageUrl: String): SyncResult {
        return repository.syncToStorage(mediaFile, storageUrl)
    }
}