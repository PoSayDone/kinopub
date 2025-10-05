package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import io.github.posaydone.filmix.core.model.FullShow

@Composable
fun PlayerShowHeader(
    showDetails: FullShow,
    currentSeason: String?,
    currentEpisode: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Show logo
        AsyncImage(
            model = showDetails.logoUrl,
            contentDescription = showDetails.title,
            modifier = Modifier
                .size(width = 300.dp, height = 80.dp),
            contentScale = ContentScale.Fit,
        )
        
        // Season and episode information
        if (currentSeason != null && currentEpisode != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$currentSeason • $currentEpisode",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
        }
    }
}