package com.androidx.codec.encoder.core.domain.usecase

import com.androidx.codec.encoder.core.domain.model.EncodeResult
import com.androidx.codec.encoder.core.domain.model.MediaFile
import com.androidx.codec.encoder.core.domain.repository.MediaRepository

class EncodeVideoUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(mediaFile: MediaFile): EncodeResult {
        return repository.encodeVideo(mediaFile)
    }
}