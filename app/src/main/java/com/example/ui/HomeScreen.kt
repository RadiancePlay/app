package com.example.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalContext
import com.example.data.update.UpdateManager
import com.example.data.update.UpdateState
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.downloads.DownloadsViewModel
import com.example.ui.games.GamesScreen
import com.example.ui.games.GamesViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.update.ForceUpdateDialog
import com.example.ui.setup.ShizukuSetupScreen
import com.example.data.shizuku.ShizukuStatus
import com.example.util.PackageHelper
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
import kotlinx.coroutines.launch

@Composable
fun RadiancePlayApp(
    gamesViewModel: GamesViewModel = viewModel(),
    downloadsViewModel: DownloadsViewModel = viewModel()
) {
    val context = LocalContext.current
    val updateManager = remember { UpdateManager() }
    val updateState by updateManager.updateState.collectAsState()

    LaunchedEffect(Unit) {
        updateManager.checkForUpdates()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val shizukuStatus by downloadsViewModel.shizukuStatus.collectAsState()
    var isBypassedForTesting by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var canInstallPackages by remember {
        mutableStateOf(PackageHelper.canRequestPackageInstalls(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                downloadsViewModel.checkShizukuStatus()
                canInstallPackages = PackageHelper.canRequestPackageInstalls(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isSetupComplete = (shizukuStatus == ShizukuStatus.READY) && canInstallPackages

    if (!isSetupComplete && !isBypassedForTesting) {
        ShizukuSetupScreen(
            status = shizukuStatus,
            canInstallPackages = canInstallPackages,
            onRequestPermission = { downloadsViewModel.requestShizukuPermission(context) },
            onRequestInstallPermission = { PackageHelper.openInstallPermissionSettings(context) },
            onCheckStatus = {
                downloadsViewModel.checkShizukuStatus()
                canInstallPackages = PackageHelper.canRequestPackageInstalls(context)
            },
            onBypassForTesting = { isBypassedForTesting = true }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("radiance_scaffold"),
            containerColor = BackgroundDark,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                RadianceBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                    }
                )
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> {
                    RadianceHomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onExploreGames = { selectedTab = 1 },
                        onVerifyShizuku = {
                            downloadsViewModel.checkShizukuStatus()
                            val isReady = downloadsViewModel.shizukuStatus.value == ShizukuStatus.READY
                            scope.launch {
                                snackbarHostState.showSnackbar(if (isReady) "Shizuku is active and authorized" else "Shizuku is not running (Setup required)")
                            }
                        }
                    )
                }
                1 -> {
                    GamesScreen(
                        viewModel = gamesViewModel,
                        modifier = Modifier.padding(innerPadding),
                        isShizukuReady = isSetupComplete,
                        onNavigateToHome = { selectedTab = 0 },
                        onRequestShizuku = {
                            isBypassedForTesting = false
                        },
                        onStartDownload = { game ->
                            val started = downloadsViewModel.startDownload(game)
                            if (started) {
                                selectedTab = 2 // Switch directly to Downloads tab so user sees the progress!
                            }
                        }
                    )
                }
                2 -> {
                    DownloadsScreen(
                        viewModel = downloadsViewModel,
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToCatalog = { selectedTab = 1 }
                    )
                }
                else -> {
                    SettingsScreen(
                        downloadsViewModel = downloadsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    if (updateState is UpdateState.UpdateRequired) {
        val updateReq = updateState as UpdateState.UpdateRequired
        ForceUpdateDialog(
            remoteVersion = updateReq.remoteVersion,
            onDownloadUpdate = {
                updateManager.openDownloadUrl(context, updateReq.downloadUrl)
            }
        )
    }
}

@Composable
fun RadianceHomeScreen(
    modifier: Modifier = Modifier,
    onExploreGames: () -> Unit,
    onVerifyShizuku: () -> Unit
) {
    var isCheckingEnv by remember { mutableStateOf(false) }
    var envChecked by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isCheckingEnv) 360f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "refresh_rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HeaderSection()
        }

        item {
            HelloWorldHeroCard(onExploreGames = onExploreGames)
        }

        item {
            EnvironmentCheckCard(
                envChecked = envChecked,
                isChecking = isCheckingEnv,
                rotationAngle = rotationAngle,
                onCheck = {
                    isCheckingEnv = true
                    envChecked = true
                    isCheckingEnv = false
                    onVerifyShizuku()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("header_section"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan.copy(alpha = 0.25f), NeonMagenta.copy(alpha = 0.25f))
                        )
                    )
                    .border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SportsEsports,
                    contentDescription = "RadiancePlay Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "RadiancePlay",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Status Badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceElevatedDark)
                .border(1.dp, SuccessGreen.copy(alpha = 0.6f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.status_engine_ready),
                    color = SuccessGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun HelloWorldHeroCard(onExploreGames: () -> Unit) {
    val neonBorderBrush = Brush.linearGradient(
        listOf(
            NeonCyan,
            NeonMagenta,
            CyberBlue
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = NeonCyan.copy(alpha = 0.35f))
            .border(1.5.dp, neonBorderBrush, RoundedCornerShape(20.dp))
            .testTag("hero_hello_world_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GAMING PLATFORM",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = RadiantGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.welcome_title),
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = stringResource(R.string.welcome_subtitle),
                color = NeonCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.welcome_desc),
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onExploreGames,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_catalog_hero_button")
            ) {
                Text(
                    text = "Explore Game Catalog",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EnvironmentCheckCard(
    envChecked: Boolean,
    isChecking: Boolean,
    rotationAngle: Float,
    onCheck: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(16.dp))
            .testTag("environment_check_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevatedDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = null,
                        tint = CyberBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Device & Environment",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCheck,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("verify_environment_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Check",
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(rotationAngle)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Check",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

            DiagnosticRow(label = "Status", value = "Ready")
            DiagnosticRow(label = "Device", value = deviceModel)
            DiagnosticRow(label = "System", value = androidVersion)

            AnimatedVisibility(visible = envChecked) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "System is ready and functioning properly.",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextTertiary, fontSize = 12.sp)
        Text(text = value, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RadianceBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(1.dp, SurfaceBorderDark)
            .testTag("radiance_bottom_navigation")
    ) {
        val items = listOf(
            Triple(0, stringResource(R.string.nav_home), Icons.Filled.SportsEsports),
            Triple(1, stringResource(R.string.nav_games), Icons.Filled.AutoAwesome),
            Triple(2, stringResource(R.string.nav_downloads), Icons.Filled.Download),
            Triple(3, stringResource(R.string.nav_settings), Icons.Filled.Settings)
        )

        items.forEach { (index, title, icon) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selectedTab == index) NeonCyan else TextTertiary
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index) NeonCyan else TextTertiary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = NeonCyan.copy(alpha = 0.15f),
                    selectedIconColor = NeonCyan,
                    unselectedIconColor = TextTertiary
                )
            )
        }
    }
}
