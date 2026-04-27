package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.model.ShowDetails

@Composable
fun PlayerMediaTitle(
    showDetails: ShowDetails,
    currentSeason: String?,
    currentEpisode: String?,
    modifier: Modifier = Modifier,
) {
    val primaryTitle = showDetails.title.trim()
    val originalTitle = showDetails.originalTitle
        .trim()
        .takeIf { it.isNotEmpty() && !it.equals(primaryTitle, ignoreCase = true) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = primaryTitle,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
        )
        if (originalTitle != null) {
            Text(
                text = originalTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
            )
        }

        if (currentSeason != null && currentEpisode != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$currentSeason • $currentEpisode",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )
        }
    }
}
