package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.posaydone.filmix.core.model.FullShow

@Composable
fun PlayerMediaTitle(
    showDetails: FullShow,
    currentSeason: String?,
    currentEpisode: String?,
    modifier: Modifier = Modifier,
    openSettingsDialog: () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column { }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                AsyncImage(
                    model = showDetails.logoUrl,
                    contentDescription = showDetails.title,
                    modifier = Modifier.width(300.dp),
                    contentScale = ContentScale.Fit,
                )

                if (currentSeason != null && currentEpisode != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$currentSeason • $currentEpisode",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1
                    )
                }
            }
            Column {
                PlayerControlsButton(
                    icon = Icons.Default.Settings, onClick = { openSettingsDialog() })
            }
        }
    }
}
