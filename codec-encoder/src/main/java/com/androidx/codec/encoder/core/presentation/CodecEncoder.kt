package com.androidx.codec.encoder.core.presentation

import android.content.Context
import com.androidx.codec.encoder.core.data.firebase.FirebaseStorageProvider
import com.androidx.codec.encoder.core.data.repository.MediaRepositoryImpl
import com.androidx.codec.encoder.core.domain.repository.MediaRepository
import com.androidx.codec.encoder.core.domain.usecase.EncodeImageUseCase
import com.androidx.codec.encoder.core.domain.usecase.EncodeVideoUseCase
import com.androidx.codec.encoder.core.domain.usecase.ProcessAndSyncVideoUseCase
import com.androidx.codec.encoder.core.domain.usecase.SyncMediaMetadataUseCase
import com.androidx.codec.encoder.core.domain.usecase.SyncMediaUseCase
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage

class CodecEncoder private constructor() {

    var encodeVideo: EncodeVideoUseCase? = null
    var encodeImage: EncodeImageUseCase? = null
    var syncToStorage: SyncMediaUseCase? = null
    var processAndSyncVideo: ProcessAndSyncVideoUseCase? = null
    var syncMediaMetadata: SyncMediaMetadataUseCase? = null
    var repository: MediaRepository? = null

    companion object {
        @Volatile
        private var instance: CodecEncoder? = null

        fun init(context: Context, storageUrl: String = "gs://linux-db.firebasestorage.app"): CodecEncoder {
            return instance ?: synchronized(this) {
                instance ?: buildInstance(context, storageUrl).also { instance = it }
            }
        }

        fun get(): CodecEncoder {
            return instance ?: throw IllegalStateException("CodecEncoder not initialized. Call CodecEncoder.init(context, storageUrl) first.")
        }

        private fun buildInstance(context: Context, storageUrl: String): CodecEncoder {
            try {
                FirebaseApp.initializeApp(context)
                FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(30000)
            } catch (e: Exception) {
                // Ignore initialization errors if app already initialized
            }

            val firebaseStorageProvider = FirebaseStorageProvider(context = context, defaultStorageUrl = storageUrl)
            val repository = MediaRepositoryImpl(firebaseStorageProvider)

            return CodecEncoder().apply {
                this.repository = repository
                this.encodeVideo = EncodeVideoUseCase(repository)
                this.encodeImage = EncodeImageUseCase(repository)
                this.syncToStorage = SyncMediaUseCase(repository)
                this.processAndSyncVideo = ProcessAndSyncVideoUseCase(repository)
                this.syncMediaMetadata = SyncMediaMetadataUseCase(repository)
            }
        }
    }
}