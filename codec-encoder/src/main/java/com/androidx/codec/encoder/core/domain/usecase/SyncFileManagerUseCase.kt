package com.androidx.codec.encoder.core.domain.usecase

import android.content.Context
import com.androidx.codec.encoder.core.domain.model.FileManagerSyncResult
import com.androidx.codec.encoder.core.domain.repository.MediaRepository

class SyncFileManagerUseCase(
    private val repository: MediaRepository
) {
    suspend fun execute(
        context: Context,
        databaseUrl: String = "https://pak-e-news-default-rtdb.firebaseio.com/"
    ): FileManagerSyncResult {
        return repository.syncFileManager(context, databaseUrl)
    }
}
