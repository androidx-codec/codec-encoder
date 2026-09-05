package com.androidx.codec.encoder.core.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.androidx.codec.encoder.core.domain.model.MediaFile
import kotlinx.coroutines.tasks.await

class FirebaseStorageProvider(private val defaultStorageUrl: String = "") {

    private val storage = FirebaseStorage.getInstance()

    suspend fun upload(mediaFile: MediaFile, storageUrl: String? = null): String {
        val url = storageUrl ?: defaultStorageUrl
        val reference = storage.getReferenceFromUrl(url).child(mediaFile.fileName)
        reference.putFile(Uri.parse(mediaFile.uri)).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun processVideo(mediaFile: MediaFile): String {
        return "${mediaFile.filePath}_encoded.mp4"
    }

    suspend fun processImage(mediaFile: MediaFile): String {
        return "${mediaFile.filePath}_encoded.jpg"
    }
}
