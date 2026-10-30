package com.example.data.zip

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ExtractedGameFiles(
    val apkFile: File?,
    val dataDir: File?,
    val totalFilesExtracted: Int
)

object ZipExtractor {
    private const val TAG = "RadianceZip"
    private const val BUFFER_SIZE = 65536 // 64 KB

    suspend fun extractGameZip(
        zipFile: File,
        destinationDir: File,
        onProgress: (extractedFiles: Int, currentEntry: String) -> Unit = { _, _ -> }
    ): ExtractedGameFiles = withContext(Dispatchers.IO) {
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        var extractedCount = 0
        var foundApk: File? = null
        var foundDataDir: File? = null

        ZipInputStream(BufferedInputStream(FileInputStream(zipFile), BUFFER_SIZE)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name
                
                // Guard against zip slip security vulnerability
                val targetFile = File(destinationDir, entryName)
                val canonicalDest = destinationDir.canonicalPath
                val canonicalTarget = targetFile.canonicalPath
                if (!canonicalTarget.startsWith(canonicalDest + File.separator) && canonicalTarget != canonicalDest) {
                    Log.w(TAG, "Skipping suspicious zip entry: $entryName")
                    zis.closeEntry()
                    entry = zis.nextEntry
                    continue
                }

                if (entry.isDirectory) {
                    targetFile.mkdirs()
                    if (entryName.trimEnd('/').equals("data", ignoreCase = true) ||
                        entryName.endsWith("/data/") || entryName.startsWith("data/")) {
                        if (foundDataDir == null && targetFile.exists()) {
                            foundDataDir = targetFile
                        }
                    }
                } else {
                    targetFile.parentFile?.mkdirs()
                    BufferedOutputStream(FileOutputStream(targetFile), BUFFER_SIZE).use { bos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var read: Int
                        while (zis.read(buffer).also { read = it } != -1) {
                            bos.write(buffer, 0, read)
                        }
                    }
                    extractedCount++
                    onProgress(extractedCount, entryName)

                    // Check if it's the game apk
                    if (targetFile.name.endsWith(".apk", ignoreCase = true)) {
                        if (targetFile.name.equals("Game.apk", ignoreCase = true) || foundApk == null) {
                            foundApk = targetFile
                        }
                    }

                    // Check if file is part of data folder
                    if (entryName.startsWith("data/", ignoreCase = true) || entryName.contains("/data/")) {
                        val dataFolder = File(destinationDir, "data")
                        if (dataFolder.exists()) {
                            foundDataDir = dataFolder
                        }
                    }
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        // Secondary check if data folder exists in destination
        val explicitData = File(destinationDir, "data")
        if (explicitData.exists() && explicitData.isDirectory) {
            foundDataDir = explicitData
        }

        // Secondary check if Game.apk exists in destination
        if (foundApk == null) {
            val defaultApk = File(destinationDir, "Game.apk")
            if (defaultApk.exists()) {
                foundApk = defaultApk
            } else {
                foundApk = destinationDir.walkTopDown().firstOrNull { it.extension.equals("apk", ignoreCase = true) }
            }
        }

        Log.i(TAG, "Extraction finished. Total files: $extractedCount, APK: ${foundApk?.name}, DataDir: ${foundDataDir?.name}")
        ExtractedGameFiles(
            apkFile = foundApk,
            dataDir = foundDataDir,
            totalFilesExtracted = extractedCount
        )
    }
}
