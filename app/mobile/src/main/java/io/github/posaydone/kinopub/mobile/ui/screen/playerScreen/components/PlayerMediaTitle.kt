package io.github.posaydone.kinopub.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.ShowDetails

@Composable
fun PlayerMediaTitle(
    showDetails: ShowDetails,
    currentSeason: String?,
    currentEpisode: String?,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    openSettingsDialog: () -> Unit,
) {
    val primaryTitle = showDetails.title.trim()
    val originalTitle = showDetails.originalTitle
        .trim()
        .takeIf { it.isNotEmpty() && !it.equals(primaryTitle, ignoreCase = true) }
    
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                PlayerControlsButton(
                    icon = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    containerColor = Color.Transparent,
                    onClick = onClose,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.Top)
            ) {
                if (originalTitle != null) {
                    Text(
                        text = originalTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                
                Text(
                    text = showDetails.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (currentSeason != null && currentEpisode != null) {
                    Text(
                        text = "$currentSeason • $currentEpisode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End
            ) {
                PlayerControlsButton(
                    icon = Icons.Default.Settings,
                    containerColor = Color.Transparent,
                    onClick = { openSettingsDialog() })
            }
        }
    }
}
