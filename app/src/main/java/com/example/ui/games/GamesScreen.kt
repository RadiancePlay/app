package com.example.ui.games

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.GameItemDto
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.RadiantGold

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    modifier: Modifier = Modifier,
    isShizukuReady: Boolean = false,
    onNavigateToHome: () -> Unit = {},
    onRequestShizuku: () -> Unit = {},
    onStartDownload: (GameItemDto) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showShizukuRequiredDialog by remember { mutableStateOf(false) }

    val handleDownloadClick: (GameItemDto) -> Unit = { game ->
        if (!isShizukuReady) {
            showShizukuRequiredDialog = true
        } else {
            onStartDownload(game)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 18.dp)
            .testTag("games_catalog_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Clean Catalog Header (no raw API URL)
        CatalogHeader(
            totalGames = uiState.filteredGames.size,
            isLoading = uiState.isLoading,
            onRefresh = { viewModel.loadGames() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.onSearchQueryChange(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Content
        if (uiState.isLoading && uiState.games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("catalog_loading_indicator"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading catalog…",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (uiState.filteredGames.isEmpty()) {
            EmptyGamesState(query = uiState.searchQuery)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("games_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.filteredGames, key = { it.id }) { game ->
                    GameCard(
                        game = game,
                        isShizukuReady = isShizukuReady,
                        onDetailsClick = { viewModel.openGameDetails(game) },
                        onDownloadClick = { handleDownloadClick(game) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Shizuku Required Alert Dialog
    if (showShizukuRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showShizukuRequiredDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = RadiantGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Setup Required", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Shizuku service and package installation permission are both required to download and install games on your device.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShizukuRequiredDialog = false
                        onRequestShizuku()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("configure_shizuku_from_catalog_button")
                ) {
                    Text("Complete Setup", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showShizukuRequiredDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevatedDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Details Dialog
    uiState.selectedGameForDetails?.let { game ->
        GameDetailsDialog(
            game = game,
            onDismiss = { viewModel.closeGameDetails() },
            onDownload = {
                viewModel.closeGameDetails()
                handleDownloadClick(game)
            }
        )
    }

    // Download Preview Dialog
    uiState.selectedGameForDownload?.let { game ->
        DownloadPreviewDialog(
            game = game,
            onDismiss = { viewModel.dismissDownloadDialog() },
            onConfirmDownload = {
                viewModel.dismissDownloadDialog()
                handleDownloadClick(game)
            }
        )
    }
}

@Composable
private fun CatalogHeader(
    totalGames: Int,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.catalog_title),
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = if (totalGames > 0) "$totalGames games available" else stringResource(R.string.catalog_subtitle),
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        IconButton(
            onClick = onRefresh,
            enabled = !isLoading,
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceElevatedDark)
                .border(1.dp, SurfaceBorderDark, CircleShape)
                .testTag("refresh_catalog_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.refresh),
                tint = if (isLoading) TextTertiary else NeonCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_search_input"),
        placeholder = {
            Text(
                text = stringResource(R.string.search_games_hint),
                color = TextTertiary,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceElevatedDark,
            unfocusedContainerColor = SurfaceDark,
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = SurfaceBorderDark,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun GameCard(
    game: GameItemDto,
    isShizukuReady: Boolean = true,
    onDetailsClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(16.dp))
            .testTag("game_item_card_${game.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Image with Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(SurfaceElevatedDark)
            ) {
                AsyncImage(
                    model = game.image,
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, SurfaceDark.copy(alpha = 0.9f))
                            )
                        )
                )

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BackgroundDark.copy(alpha = 0.85f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "v${game.version}",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    game.language?.let { lang ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BackgroundDark.copy(alpha = 0.85f))
                                .border(1.dp, NeonMagenta.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = null,
                                    tint = NeonMagenta,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = lang.uppercase(),
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = game.title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDetailsClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("details_button_${game.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(SurfaceBorderDark, SurfaceBorderDark))
                        )
                    ) {
                        Text("Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("download_button_${game.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = BackgroundDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.download),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGamesState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("empty_games_state"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (query.isEmpty()) "No games available" else "No games match \"$query\"",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try pulling down to refresh or check your search keyword.",
                color = TextTertiary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun GameDetailsDialog(
    game: GameItemDto,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("game_details_dialog"),
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
                    Text(
                        text = stringResource(R.string.game_details),
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = game.title,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                DetailItem(label = "Version", value = "v${game.version}")
                DetailItem(label = "Language", value = game.language?.uppercase() ?: "GLOBAL")

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.close), color = TextSecondary)
                    }

                    Button(
                        onClick = onDownload,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("dialog_download_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = BackgroundDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.download), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadPreviewDialog(
    game: GameItemDto,
    onDismiss: () -> Unit,
    onConfirmDownload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Download, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Download Game", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                text = "Would you like to download and install \"${game.title}\"?",
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDownload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_download_dialog_button")
            ) {
                Text(stringResource(R.string.download), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceElevatedDark,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextTertiary, fontSize = 13.sp)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
