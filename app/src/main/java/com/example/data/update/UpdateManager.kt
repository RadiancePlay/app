package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.RadiancePlayApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpdateRequired(val remoteVersion: String, val downloadUrl: String) : UpdateState()
    data object UpToDate : UpdateState()
}

class UpdateManager(
    private val api: RadiancePlayApi = RadiancePlayApi.create()
) {
    private val TAG = "RadianceUpdate"
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    suspend fun checkForUpdates() = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Checking
        try {
            val response = api.checkAppVersion()
            val currentVersion = BuildConfig.VERSION_NAME
            Log.i(TAG, "Checking update. Current: $currentVersion, Remote: ${response.version}")

            if (isVersionNewer(current = currentVersion, remote = response.version)) {
                Log.w(TAG, "Update required: ${response.version} (Download: ${response.download})")
                _updateState.value = UpdateState.UpdateRequired(
                    remoteVersion = response.version,
                    downloadUrl = response.download
                )
            } else {
                _updateState.value = UpdateState.UpToDate
            }
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed or offline: ${e.message}")
            _updateState.value = UpdateState.UpToDate
        }
    }

    fun openDownloadUrl(context: Context, downloadUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch browser for update: ${e.message}")
        }
    }

    companion object {
        fun isVersionNewer(current: String, remote: String): Boolean {
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
            val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(currentParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val curr = currentParts.getOrElse(i) { 0 }
                val rem = remoteParts.getOrElse(i) { 0 }
                if (rem > curr) return true
                if (rem < curr) return false
            }
            return false
        }
    }
}
