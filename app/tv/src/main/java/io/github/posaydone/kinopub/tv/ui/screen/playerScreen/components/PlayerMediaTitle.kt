package io.github.posaydone.kinopub.tv.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.kinopub.core.model.ShowDetails

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
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
    ) {
        if (originalTitle != null) {
            Text(
                text = originalTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
            )
        }
        
        
        Text(
            text = primaryTitle,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1,
        )

        if (currentSeason != null && currentEpisode != null) {
            Text(
                text = "$currentSeason • $currentEpisode",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
            )
        }
    }
}
