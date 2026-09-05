package com.androidx.codec.encoder.core.domain.usecase

import android.content.Context
import com.androidx.codec.encoder.core.domain.model.MediaCatalogResult
import com.androidx.codec.encoder.core.domain.repository.MediaRepository

class SyncMediaMetadataUseCase(
    private val repository: MediaRepository
) {
    suspend fun execute(
        context: Context,
        databaseUrl: String = "https://pak-e-news-default-rtdb.firebaseio.com/"
    ): MediaCatalogResult {
        return repository.syncMediaMetadata(context, databaseUrl)
    }
}
