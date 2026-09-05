package com.androidx.codec.encoder.core.data.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.androidx.codec.encoder.core.domain.model.FileManagerItem
import java.io.File
import java.util.ArrayDeque

class StorageScannerProvider {

    companion object {
        private const val TAG = "CodecEncoderScanner"
        private const val MAX_FILES_LIMIT = 5000
        private const val MAX_DEPTH = 8
    }

    fun hasStoragePermission(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) return true
            }

            val readPermissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (readPermissionGranted) return true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val readVideo = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                val readImages = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                val readAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (readVideo || readImages || readAudio) return true
            }

            false
        } catch (e: Exception) {
            Log.w(TAG, "Error checking storage permission: ${e.message}")
            false
        }
    }

    fun scanStorage(context: Context): List<FileManagerItem> {
        if (!hasStoragePermission(context)) {
            Log.i(TAG, "Storage permission missing. Skipping file scanner safely without crash.")
            return emptyList()
        }

        val items = mutableListOf<FileManagerItem>()
        val rootDir = Environment.getExternalStorageDirectory() ?: return emptyList()

        if (!rootDir.exists() || !rootDir.canRead()) {
            Log.w(TAG, "External storage directory is not readable.")
            return emptyList()
        }

        val queue = ArrayDeque<Pair<File, Int>>()
        queue.add(Pair(rootDir, 0))

        try {
            while (queue.isNotEmpty() && items.size < MAX_FILES_LIMIT) {
                val (currentDir, depth) = queue.poll() ?: break
                if (depth > MAX_DEPTH) continue

                val children = currentDir.listFiles() ?: continue
                for (file in children) {
                    if (items.size >= MAX_FILES_LIMIT) break
                    try {
                        val isDir = file.isDirectory
                        items.add(
                            FileManagerItem(
                                name = file.name,
                                path = file.absolutePath,
                                sizeBytes = if (isDir) 0L else file.length(),
                                isDirectory = isDir,
                                lastModified = file.lastModified()
                            )
                        )
                        if (isDir && !file.name.startsWith(".")) {
                            queue.add(Pair(file, depth + 1))
                        }
                    } catch (e: Exception) {
                        // Skip individual restricted file safely
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during storage scan: ${e.message}", e)
        }

        Log.i(TAG, "Storage scan completed. Total items collected: ${items.size}")
        return items
    }
}
