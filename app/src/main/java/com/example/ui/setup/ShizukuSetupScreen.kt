package com.example.ui.setup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.shizuku.ShizukuStatus
import com.example.ui.theme.BackgroundDark
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
import com.example.util.PackageHelper

@Composable
fun ShizukuSetupScreen(
    status: ShizukuStatus,
    canInstallPackages: Boolean,
    onRequestPermission: () -> Unit,
    onRequestInstallPermission: () -> Unit,
    onCheckStatus: () -> Unit,
    onBypassForTesting: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isChecking) 360f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "refresh_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 22.dp)
            .testTag("shizuku_setup_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan.copy(alpha = 0.25f), NeonMagenta.copy(alpha = 0.25f))
                        )
                    )
                    .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SportsEsports,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "RadiancePlay Setup",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Authorize Shizuku and package installation to enable downloading and running games on your device.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Status Card
            SetupOverviewCard(
                shizukuStatus = status,
                canInstallPackages = canInstallPackages
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Setup Step 1: Open Shizuku
            StepCard(
                stepNumber = "1",
                icon = Icons.Filled.Launch,
                title = "Open Shizuku",
                description = "Launch the Shizuku app on your device, or download it if not yet installed.",
                actionLabel = "Open App",
                isDone = status == ShizukuStatus.READY,
                onAction = { openShizukuApp(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Setup Step 2: Start Shizuku Service
            StepCard(
                stepNumber = "2",
                icon = Icons.Filled.Terminal,
                title = "Start Shizuku Service",
                description = "Activate Shizuku via Wireless Debugging or Root inside the Shizuku app.",
                actionLabel = null,
                isDone = status == ShizukuStatus.READY || status == ShizukuStatus.PERMISSION_REQUIRED,
                onAction = null
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Setup Step 3: Authorize Shizuku Access
            StepCard(
                stepNumber = "3",
                icon = Icons.Filled.Security,
                title = "Authorize Shizuku",
                description = "Grant RadiancePlay access to deploy game data into protected system folders.",
                actionLabel = if (status == ShizukuStatus.READY) "Authorized" else "Grant Access",
                isDone = status == ShizukuStatus.READY,
                onAction = onRequestPermission
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Setup Step 4: Authorize Package Installs
            StepCard(
                stepNumber = "4",
                icon = Icons.Filled.InstallMobile,
                title = "Allow App Installs",
                description = "Authorize RadiancePlay to install games normally using the Android Package Installer.",
                actionLabel = if (canInstallPackages) "Authorized" else "Authorize",
                isDone = canInstallPackages,
                onAction = onRequestInstallPermission
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Check Status Button
            Button(
                onClick = {
                    isChecking = true
                    onCheckStatus()
                    isChecking = false

                    if (status != ShizukuStatus.READY && !canInstallPackages) {
                        Toast.makeText(context, "Shizuku and Install permission are both required.", Toast.LENGTH_SHORT).show()
                    } else if (status != ShizukuStatus.READY) {
                        Toast.makeText(context, "Shizuku service must be running and authorized.", Toast.LENGTH_SHORT).show()
                    } else if (!canInstallPackages) {
                        Toast.makeText(context, "Please allow 'Install unknown apps' permission.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_shizuku_setup_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotationAngle)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Verify & Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bypass option when user continues without Shizuku
            TextButton(
                onClick = onBypassForTesting,
                modifier = Modifier.testTag("bypass_setup_button")
            ) {
                Text(
                    text = "Continue without Shizuku (Unable to download games)",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SetupOverviewCard(
    shizukuStatus: ShizukuStatus,
    canInstallPackages: Boolean
) {
    val isShizukuOk = shizukuStatus == ShizukuStatus.READY
    val isAllOk = isShizukuOk && canInstallPackages

    val borderColor = if (isAllOk) SuccessGreen else RadiantGold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StatusItemRow(
                label = "Shizuku Service",
                isOk = isShizukuOk,
                statusText = when (shizukuStatus) {
                    ShizukuStatus.READY -> "Connected"
                    ShizukuStatus.PERMISSION_REQUIRED -> "Needs Authorization"
                    else -> "Not Running"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusItemRow(
                label = "Install Permission",
                isOk = canInstallPackages,
                statusText = if (canInstallPackages) "Allowed" else "Action Required"
            )
        }
    }
}

@Composable
private fun StatusItemRow(
    label: String,
    isOk: Boolean,
    statusText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isOk) SuccessGreen else RadiantGold)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isOk) SuccessGreen.copy(alpha = 0.15f) else RadiantGold.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = statusText,
                color = if (isOk) SuccessGreen else RadiantGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String?,
    isDone: Boolean = false,
    onAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDone) SuccessGreen.copy(alpha = 0.4f) else SurfaceBorderDark,
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevatedDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isDone) SuccessGreen.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = stepNumber,
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (actionLabel != null && onAction != null && !isDone) {
                    OutlinedButton(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(NeonCyan, NeonCyan))
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(text = actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isDone) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "DONE",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private fun openShizukuApp(context: Context) {
    val shizukuPackage = "moe.shizuku.privileged.api"
    val launchIntent = context.packageManager.getLaunchIntentForPackage(shizukuPackage)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        try {
            val playStoreIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$shizukuPackage")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$shizukuPackage")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(webIntent)
        }
    }
}
