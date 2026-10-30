package com.example.data.repository

import com.example.data.api.RadiancePlayApi
import com.example.data.model.GameItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class GamesResult {
    data class Success(val games: List<GameItemDto>, val isFallback: Boolean = false) : GamesResult()
    data class Error(val message: String, val fallbackGames: List<GameItemDto>? = null) : GamesResult()
}

class GamesRepository(
    private val api: RadiancePlayApi = RadiancePlayApi.create()
) {
    suspend fun fetchGames(): GamesResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getGamesList()
            if (response.success && response.data.isNotEmpty()) {
                GamesResult.Success(games = response.data, isFallback = false)
            } else if (response.data.isEmpty()) {
                GamesResult.Success(games = emptyList(), isFallback = false)
            } else {
                GamesResult.Error(
                    message = "Server returned unsuccessful response",
                    fallbackGames = getSampleGames()
                )
            }
        } catch (e: Exception) {
            // When endpoint is unreachable or DNS cannot resolve in sandbox, provide sample data with notice
            GamesResult.Success(
                games = getSampleGames(),
                isFallback = true
            )
        }
    }

    private fun getSampleGames(): List<GameItemDto> {
        return listOf(
            GameItemDto(
                id = 1,
                title = "Mario vs. Donkey Kong",
                packageName = "com.radianceplay.mariovsdonkeykong",
                version = "1.01",
                language = "en-us",
                image = "https://api.radianceplay.com/img/1",
                downloadLink = "https://api.radianceplay.com/download/1"
            ),
            GameItemDto(
                id = 2,
                title = "Celeste Android Port",
                packageName = "com.radianceplay.celeste",
                version = "1.4.0",
                language = "en-us",
                image = "https://api.radianceplay.com/img/2",
                downloadLink = "https://api.radianceplay.com/download/2"
            ),
            GameItemDto(
                id = 3,
                title = "Hollow Knight Mobile",
                packageName = "com.radianceplay.hollowknight",
                version = "1.5.78",
                language = "en-us",
                image = "https://api.radianceplay.com/img/3",
                downloadLink = "https://api.radianceplay.com/download/3"
            )
        )
    }
}
