package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import io.github.posaydone.filmix.core.model.ShowDetails

@Composable
fun PlayerMediaTitle(
    showDetails: ShowDetails,
    currentSeason: String?,
    currentEpisode: String?,
    modifier: Modifier = Modifier,
    openSettingsDialog: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {}

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = showDetails.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color = Color.White,
                )

                if (currentSeason != null && currentEpisode != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$currentSeason • $currentEpisode",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End
            ) {
                PlayerControlsButton(
                    icon = Icons.Default.Settings, onClick = { openSettingsDialog() })
            }
        }
    }
}
