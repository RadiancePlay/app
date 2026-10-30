package com.example.ui.downloads

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.data.download.DownloadEngine
import com.example.data.download.DownloadTask
import com.example.data.model.GameItemDto
import com.example.data.shizuku.ShizukuBridge
import com.example.data.shizuku.ShizukuStatus
import kotlinx.coroutines.flow.StateFlow

import com.example.util.PackageHelper

class DownloadsViewModel(application: Application) : AndroidViewModel(application) {
    val downloadEngine = DownloadEngine(application.applicationContext)
    val tasks: StateFlow<Map<Long, DownloadTask>> = downloadEngine.tasks
    val shizukuStatus: StateFlow<ShizukuStatus> = ShizukuBridge.status

    init {
        ShizukuBridge.init()
    }

    fun startDownload(game: GameItemDto): Boolean {
        val hasInstallPermission = PackageHelper.canRequestPackageInstalls(getApplication())
        if (ShizukuBridge.status.value != ShizukuStatus.READY || !hasInstallPermission) {
            return false
        }
        downloadEngine.startDownload(game)
        return true
    }

    fun cancelDownload(gameId: Long) {
        downloadEngine.cancelDownload(gameId)
    }

    fun requestShizukuPermission(context: Context? = null) {
        ShizukuBridge.requestPermission(context)
    }

    fun checkShizukuStatus() {
        ShizukuBridge.checkStatus()
    }

    fun launchGame(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(
                context,
                "Starting game $packageName…",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
