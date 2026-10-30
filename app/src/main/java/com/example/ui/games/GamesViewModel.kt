package com.example.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GameItemDto
import com.example.data.repository.GamesRepository
import com.example.data.repository.GamesResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GamesUiState(
    val isLoading: Boolean = false,
    val games: List<GameItemDto> = emptyList(),
    val filteredGames: List<GameItemDto> = emptyList(),
    val searchQuery: String = "",
    val isFallback: Boolean = false,
    val errorMessage: String? = null,
    val selectedGameForDetails: GameItemDto? = null,
    val selectedGameForDownload: GameItemDto? = null
)

class GamesViewModel(
    private val repository: GamesRepository = GamesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesUiState(isLoading = true))
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.fetchGames()) {
                is GamesResult.Success -> {
                    _uiState.update { current ->
                        val filtered = filterGames(result.games, current.searchQuery)
                        current.copy(
                            isLoading = false,
                            games = result.games,
                            filteredGames = filtered,
                            isFallback = result.isFallback,
                            errorMessage = null
                        )
                    }
                }
                is GamesResult.Error -> {
                    val fallback = result.fallbackGames ?: emptyList()
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            games = fallback,
                            filteredGames = filterGames(fallback, current.searchQuery),
                            isFallback = true,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = newQuery,
                filteredGames = filterGames(current.games, newQuery)
            )
        }
    }

    fun openGameDetails(game: GameItemDto) {
        _uiState.update { it.copy(selectedGameForDetails = game) }
    }

    fun closeGameDetails() {
        _uiState.update { it.copy(selectedGameForDetails = null) }
    }

    fun selectGameForDownload(game: GameItemDto) {
        _uiState.update { it.copy(selectedGameForDownload = game) }
    }

    fun dismissDownloadDialog() {
        _uiState.update { it.copy(selectedGameForDownload = null) }
    }

    private fun filterGames(games: List<GameItemDto>, query: String): List<GameItemDto> {
        if (query.isBlank()) return games
        val lower = query.trim().lowercase()
        return games.filter {
            it.title.lowercase().contains(lower) ||
            it.packageName.lowercase().contains(lower) ||
            (it.language?.lowercase()?.contains(lower) == true)
        }
    }
}
