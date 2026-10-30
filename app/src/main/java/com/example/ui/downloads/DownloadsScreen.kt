package com.example.ui.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.download.DownloadStage
import com.example.data.download.DownloadTask
import com.example.data.shizuku.ShizukuStatus
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RadiantGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    modifier: Modifier = Modifier,
    onNavigateToCatalog: () -> Unit
) {
    val context = LocalContext.current
    val tasksMap by viewModel.tasks.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val tasksList = tasksMap.values.toList().reversed()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 18.dp)
            .testTag("downloads_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header
        DownloadsHeader(
            activeCount = tasksList.count { it.stage != DownloadStage.INSTALLED && it.stage != DownloadStage.FAILED },
            shizukuStatus = shizukuStatus,
            onRequestShizuku = { viewModel.requestShizukuPermission() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Shizuku Notice Banner
        ShizukuBanner(
            status = shizukuStatus,
            onRequestPermission = { viewModel.requestShizukuPermission() },
            onCheck = { viewModel.checkShizukuStatus() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (tasksList.isEmpty()) {
            EmptyDownloadsState(onNavigateToCatalog = onNavigateToCatalog)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("downloads_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tasksList, key = { it.game.id }) { task ->
                    DownloadTaskCard(
                        task = task,
                        onCancel = { viewModel.cancelDownload(task.game.id) },
                        onRetry = { viewModel.startDownload(task.game) },
                        onLaunch = { viewModel.launchGame(context, task.game.packageName) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DownloadsHeader(
    activeCount: Int,
    shizukuStatus: ShizukuStatus,
    onRequestShizuku: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Downloads",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = if (activeCount > 0) "$activeCount in progress" else "No active downloads",
                color = if (activeCount > 0) NeonCyan else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceElevatedDark)
                .border(
                    1.dp,
                    if (shizukuStatus == ShizukuStatus.READY) SuccessGreen.copy(alpha = 0.7f) else RadiantGold.copy(alpha = 0.7f),
                    CircleShape
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (shizukuStatus == ShizukuStatus.READY) SuccessGreen else RadiantGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (shizukuStatus == ShizukuStatus.READY) "SHIZUKU ACTIVE" else "SHIZUKU STANDBY",
                    color = if (shizukuStatus == ShizukuStatus.READY) SuccessGreen else RadiantGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ShizukuBanner(
    status: ShizukuStatus,
    onRequestPermission: () -> Unit,
    onCheck: () -> Unit
) {
    val (bgColor, borderColor, iconTint, title, desc, actionText) = when (status) {
        ShizukuStatus.READY -> Tuple6(
            Color(0xFF07271F),
            NeonCyan.copy(alpha = 0.4f),
            NeonCyan,
            "Shizuku Ready",
            "Automatic silent installation is active.",
            null
        )
        ShizukuStatus.PERMISSION_REQUIRED -> Tuple6(
            SurfaceElevatedDark,
            RadiantGold.copy(alpha = 0.6f),
            RadiantGold,
            "Shizuku Permission Required",
            "Grant permission to enable automatic background installation.",
            "Enable"
        )
        else -> Tuple6(
            SurfaceDark,
            SurfaceBorderDark,
            CyberBlue,
            "Shizuku Standby",
            "Downloaded games require Shizuku to install automatically.",
            null
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.Terminal,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            if (actionText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = if (status == ShizukuStatus.PERMISSION_REQUIRED) onRequestPermission else onCheck,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconTint,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("shizuku_action_button")
                ) {
                    Text(text = actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DownloadTaskCard(
    task: DownloadTask,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                when (task.stage) {
                    DownloadStage.INSTALLED -> SuccessGreen.copy(alpha = 0.5f)
                    DownloadStage.FAILED -> Color(0xFFEF4444).copy(alpha = 0.5f)
                    else -> NeonCyan.copy(alpha = 0.5f)
                },
                RoundedCornerShape(16.dp)
            )
            .testTag("task_card_${task.game.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and stage badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.game.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = task.game.packageName,
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                StageBadge(stage = task.stage)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { task.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = when (task.stage) {
                    DownloadStage.INSTALLED -> SuccessGreen
                    DownloadStage.FAILED -> Color(0xFFEF4444)
                    else -> NeonCyan
                },
                trackColor = SurfaceElevatedDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status message
            Text(
                text = task.statusMessage,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (task.stage) {
                    DownloadStage.INSTALLED -> {
                        Button(
                            onClick = onLaunch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = BackgroundDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("launch_game_button_${task.game.id}")
                        ) {
                            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Game", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    DownloadStage.FAILED -> {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RadiantGold,
                                contentColor = BackgroundDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("retry_task_button_${task.game.id}")
                        ) {
                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("cancel_task_button_${task.game.id}")
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageBadge(stage: DownloadStage) {
    val (text, color) = when (stage) {
        DownloadStage.QUEUED -> "QUEUED" to TextTertiary
        DownloadStage.DOWNLOADING -> "DOWNLOADING" to NeonCyan
        DownloadStage.EXTRACTING -> "EXTRACTING" to CyberBlue
        DownloadStage.DEPLOYING_DATA -> "DEPLOYING DATA" to NeonMagenta
        DownloadStage.INSTALLING_APK -> "SHIZUKU INSTALL" to RadiantGold
        DownloadStage.INSTALLED -> "READY TO PLAY" to SuccessGreen
        DownloadStage.FAILED -> "FAILED" to Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyDownloadsState(onNavigateToCatalog: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("empty_downloads_state"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevatedDark)
                    .border(1.dp, SurfaceBorderDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Downloads Active",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Browse the game catalog and download your favorite titles with automated extraction and Shizuku deployment.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onNavigateToCatalog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("browse_catalog_button")
            ) {
                Icon(imageVector = Icons.Filled.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Browse Game Catalog", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
