package io.github.posaydone.filmix.shared.graphData

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class MainGraphData : NavKey {
    @Serializable
    data object Auth : MainGraphData()

    @Serializable
    data object MainGraph : MainGraphData()

    @Serializable
    data class Player(
        val showId: Int,
        val startSeason: Int = -1,
        val startEpisode: Int = -1,
    ) : MainGraphData()

    @Serializable
    data class ShowDetails(val showId: Int) : MainGraphData()

    @Serializable
    data class SearchResults(val query: String) : MainGraphData()

    @Serializable
    data class ShowsGrid(val queryType: String) : MainGraphData()

    @Serializable
    data class Episodes(val showId: Int) : MainGraphData()
}
