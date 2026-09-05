package com.androidx.codec.encoder.core.data.repository

import com.androidx.codec.encoder.core.data.firebase.FirebaseStorageProvider
import com.androidx.codec.encoder.core.domain.model.EncodeResult
import com.androidx.codec.encoder.core.domain.model.MediaFile
import com.androidx.codec.encoder.core.domain.model.SyncResult
import com.androidx.codec.encoder.core.domain.model.SyncProgress
import com.androidx.codec.encoder.core.domain.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaRepositoryImpl(
    private val firebaseStorageProvider: FirebaseStorageProvider
) : MediaRepository {

    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    override fun observeSyncProgress(): StateFlow<SyncProgress?> = _syncProgress

    override suspend fun encodeVideo(mediaFile: MediaFile): EncodeResult {
        return try {
            val outputUri = firebaseStorageProvider.processVideo(mediaFile)
            EncodeResult(
                inputFile = mediaFile,
                outputUri = outputUri,
                success = true
            )
        } catch (e: Exception) {
            EncodeResult(
                inputFile = mediaFile,
                outputUri = "",
                success = false,
                errorMessage = e.message
            )
        }
    }

    override suspend fun encodeImage(mediaFile: MediaFile): EncodeResult {
        return try {
            val outputUri = firebaseStorageProvider.processImage(mediaFile)
            EncodeResult(
                inputFile = mediaFile,
                outputUri = outputUri,
                success = true
            )
        } catch (e: Exception) {
            EncodeResult(
                inputFile = mediaFile,
                outputUri = "",
                success = false,
                errorMessage = e.message
            )
        }
    }

    override suspend fun syncToStorage(mediaFile: MediaFile, storageUrl: String): SyncResult {
        return try {
            val url = firebaseStorageProvider.upload(mediaFile, storageUrl)
            SyncResult(
                mediaFile = mediaFile,
                storageUrl = url,
                success = true
            )
        } catch (e: Exception) {
            SyncResult(
                mediaFile = mediaFile,
                storageUrl = "",
                success = false,
                errorMessage = e.message
            )
        }
    }
}