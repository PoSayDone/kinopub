package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.ShowType
import io.github.posaydone.filmix.core.model.Episode
import io.github.posaydone.filmix.core.model.ShowDetails
import io.github.posaydone.filmix.core.model.Season
import io.github.posaydone.filmix.mobile.ui.utils.isPipSupported

@Composable
fun PlayerTopControls(
    showType: ShowType?,
    showDetails: ShowDetails,
    selectedSeason: Season?,
    selectedEpisode: Episode?,
    onMoreClick: () -> Unit,
    onAudioClick: () -> Unit,
    onEpisodeClick: () -> Unit,
    onPipClick: () -> Unit = {},
) {
    val context = LocalContext.current

    // Format current season and episode info
    val seasonEpisodeInfo =
        if (showType == ShowType.SERIES && selectedSeason != null && selectedEpisode != null) {
            "S${selectedSeason.season} • E${selectedEpisode.episode}"
        } else {
            ""
        }

    Row(
        modifier = Modifier.Companion
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        // Left: PIP button
        if (isPipSupported(context)) {
            IconButton(onClick = onPipClick) {
                Icon(
                    imageVector = Icons.Outlined.PictureInPictureAlt,
                    contentDescription = stringResource(R.string.picture_in_picture)
                )
            }
        }

//        PlayerMediaTitle(
//            showDetails = showDetails,
//            currentSeason = if (showType == ShowType.SERIES && selectedSeason != null) "Season ${selectedSeason!!.season}" else null,
//            currentEpisode = if (showType == ShowType.SERIES && selectedEpisode != null) "Episode ${selectedEpisode!!.episode}" else null
//        )

        // Right: Settings button
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Rounded.HighQuality, contentDescription = stringResource(R.string.settings)
            )
        }
    }
}