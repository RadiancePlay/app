package com.example.data.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

enum class ShizukuStatus {
    UNKNOWN,
    NOT_INSTALLED,
    NOT_RUNNING,
    PERMISSION_REQUIRED,
    READY
}

data class ShellResult(
    val exitCode: Int,
    val output: String,
    val error: String
)

object ShizukuBridge {
    private const val TAG = "RadianceShizuku"
    const val REQUEST_CODE_SHIZUKU_PERMISSION = 7120

    private val _status = MutableStateFlow(ShizukuStatus.UNKNOWN)
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _status.value = ShizukuStatus.NOT_RUNNING
    }

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_SHIZUKU_PERMISSION) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    _status.value = ShizukuStatus.READY
                } else {
                    _status.value = ShizukuStatus.PERMISSION_REQUIRED
                }
            }
        }

    fun init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            checkStatus()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize Shizuku listeners", e)
            _status.value = ShizukuStatus.NOT_RUNNING
        }
    }

    fun checkStatus(): ShizukuStatus {
        val newStatus = try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.isPreV11()) {
                    ShizukuStatus.READY
                } else {
                    val granted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                    if (granted) ShizukuStatus.READY else ShizukuStatus.PERMISSION_REQUIRED
                }
            } else {
                ShizukuStatus.NOT_RUNNING
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Shizuku not reachable: ${e.message}")
            ShizukuStatus.NOT_RUNNING
        }
        _status.value = newStatus
        return newStatus
    }

    fun requestPermission(context: Context? = null) {
        try {
            if (!Shizuku.pingBinder()) {
                Log.w(TAG, "Cannot request permission: Shizuku binder is not running.")
                context?.let {
                    android.widget.Toast.makeText(
                        it,
                        "Shizuku service is not running. Please start Shizuku first.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                checkStatus()
                return
            }
            if (Shizuku.isPreV11()) {
                _status.value = ShizukuStatus.READY
                return
            }
            val isGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                _status.value = ShizukuStatus.READY
            } else {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU_PERMISSION)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error requesting Shizuku permission", e)
            checkStatus()
        }
    }

    suspend fun executeShellCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process: Process = try {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                ).apply { isAccessible = true }
                newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            } catch (e: Throwable) {
                Log.w(TAG, "Shizuku.newProcess unavailable via reflection, falling back to local shell: ${e.message}")
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }

            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val error = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()
            ShellResult(exitCode, output.trim(), error.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Shell command error for: $command", e)
            ShellResult(-1, "", e.message ?: "Execution failed")
        }
    }

    suspend fun executeShellCommandWithStreamInput(command: String, inputFile: File): ShellResult = withContext(Dispatchers.IO) {
        try {
            val process: Process = try {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                ).apply { isAccessible = true }
                newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            } catch (e: Throwable) {
                Log.w(TAG, "Shizuku.newProcess unavailable via reflection, falling back to local shell: ${e.message}")
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }

            // Pipe inputFile bytes into the process stdin in a background coroutine
            val writeJob = launch(Dispatchers.IO) {
                try {
                    BufferedInputStream(FileInputStream(inputFile), 65536).use { input ->
                        BufferedOutputStream(process.outputStream, 65536).use { output ->
                            input.copyTo(output)
                            output.flush()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error streaming input to process: ${e.message}")
                }
            }

            val outDeferred = async(Dispatchers.IO) {
                BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            }
            val errDeferred = async(Dispatchers.IO) {
                BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            }

            writeJob.join()
            val output = outDeferred.await()
            val error = errDeferred.await()
            val exitCode = process.waitFor()

            ShellResult(exitCode, output.trim(), error.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Shell stream error for command: $command", e)
            ShellResult(-1, "", e.message ?: "Execution failed")
        }
    }

    suspend fun installApk(apkFile: File): ShellResult = withContext(Dispatchers.IO) {
        if (!apkFile.exists()) {
            return@withContext ShellResult(-1, "", "APK file does not exist: ${apkFile.absolutePath}")
        }

        val apkSize = apkFile.length()
        val tempApkName = "radiance_install_${System.currentTimeMillis()}.apk"
        val tempPath = "/data/local/tmp/$tempApkName"

        Log.i(TAG, "Starting Shizuku APK installation for: ${apkFile.name} ($apkSize bytes)")

        // Method 1: Stream APK via Shizuku shell process stdin into /data/local/tmp, then run pm install
        val stageAndInstallCmd = "cat > \"$tempPath\" && pm install -r -d -g -t \"$tempPath\"; EXIT_CODE=\$?; rm -f \"$tempPath\"; exit \$EXIT_CODE"
        val result = executeShellCommandWithStreamInput(stageAndInstallCmd, apkFile)
        Log.i(TAG, "Method 1 (stage & install) result: exitCode=${result.exitCode}, out='${result.output}', err='${result.error}'")

        if (result.exitCode == 0 || result.output.contains("Success", ignoreCase = true)) {
            return@withContext ShellResult(0, result.output.ifEmpty { "Success" }, result.error)
        }

        // Method 2: Direct streaming to pm install -S
        Log.w(TAG, "Method 1 did not succeed (${result.error}), trying 'pm install -S' stream fallback...")
        val directCmd = "pm install -r -d -g -t -S $apkSize"
        val directResult = executeShellCommandWithStreamInput(directCmd, apkFile)
        Log.i(TAG, "Method 2 (direct pm -S) result: exitCode=${directResult.exitCode}, out='${directResult.output}', err='${directResult.error}'")

        if (directResult.exitCode == 0 || directResult.output.contains("Success", ignoreCase = true)) {
            return@withContext ShellResult(0, directResult.output.ifEmpty { "Success" }, directResult.error)
        }

        // Return best result
        if (result.error.isNotEmpty()) result else directResult
    }

    suspend fun deployGameData(extractedDataDir: File, packageName: String): ShellResult = withContext(Dispatchers.IO) {
        if (!extractedDataDir.exists() || !extractedDataDir.isDirectory) {
            return@withContext ShellResult(0, "No data folder found to copy", "")
        }

        val targetPath = "/sdcard/Android/data/$packageName/files"
        Log.i(TAG, "Deploying game data from ${extractedDataDir.absolutePath} to $targetPath")

        // 1. Create target folder /Android/data/<package>/files/
        val mkdirCmd = "mkdir -p \"$targetPath\""
        val mkdirRes = executeShellCommand(mkdirCmd)
        if (mkdirRes.exitCode != 0) {
            Log.w(TAG, "mkdir warning: ${mkdirRes.error}")
        }

        // 2. Copy all contents inside extracted data/ folder directly into /Android/data/<package>/files/
        val copyCmd = "cp -r \"${extractedDataDir.absolutePath}/\"* \"$targetPath/\""
        val copyRes = executeShellCommand(copyCmd)

        // 3. Ensure permissions are set for the target files
        val chmodCmd = "chmod -R 777 \"$targetPath\""
        executeShellCommand(chmodCmd)

        copyRes
    }
}
