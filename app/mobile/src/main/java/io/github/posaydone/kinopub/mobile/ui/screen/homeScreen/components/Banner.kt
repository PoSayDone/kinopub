package io.github.posaydone.kinopub.mobile.ui.screen.homeScreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.Show
import io.github.posaydone.kinopub.core.model.ShowProgress
import io.github.posaydone.kinopub.core.model.latestProgressItem
import io.github.posaydone.kinopub.core.model.latestSeriesProgress
import io.github.posaydone.kinopub.mobile.ui.common.ShowBannerContent
import io.github.posaydone.kinopub.mobile.ui.common.ShowPoster
import io.github.posaydone.kinopub.mobile.ui.utils.bottomBorder
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBanner(
    modifier: Modifier = Modifier,
    featuredShow: Show,
    featuredShowProgress: ShowProgress,
    navigateToMoviePlayer: (showId: Int, startSeason: Int, startEpisode: Int) -> Unit,
) {
    val headerHeight = 420.dp
    val playProgress = if (featuredShow.isSeries) {
        featuredShowProgress.latestSeriesProgress()
    } else {
        featuredShowProgress.latestProgressItem()
    }
    val playButtonText = when {
        featuredShow.isSeries && playProgress != null -> stringResource(
            R.string.continueWatchingSeries,
            playProgress.season,
            playProgress.episode,
        )
        !featuredShow.isSeries && playProgress != null -> stringResource(R.string.continueWatchingMovie)
        else -> stringResource(R.string.playString)
    }

    Box(modifier = modifier) {
        ShowPoster(
            backdropUrl = featuredShow.backdropUrl,
            posterUrl = featuredShow.poster,
            height = headerHeight,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .bottomBorder(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Spacer(modifier = Modifier.height(headerHeight - 100.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ShowBannerContent(
                    title = featuredShow.title.trim(),
                    originalTitle = featuredShow.originalTitle
                        .trim()
                        .takeIf { it.isNotEmpty() && !it.equals(featuredShow.title.trim(), ignoreCase = true) },
                    logoUrl = null,
                    ratingKp = featuredShow.ratingKp,
                    votesKp = featuredShow.votesKp,
                    year = featuredShow.year,
                    genres = featuredShow.genres.map { it.name },
                    countries = featuredShow.countries.map { it.name },
                    durationSeconds = featuredShow.durationSeconds?.takeIf { !featuredShow.isSeries && it > 0 },
                    ageRating = featuredShow.ageRating.takeIf { it > 0 },
                    description = featuredShow.description,
                    maxDescriptionLines = 3,
                    onPlayClick = {
                        navigateToMoviePlayer(
                            featuredShow.id,
                            playProgress?.season ?: -1,
                            playProgress?.episode ?: -1,
                        )
                    },
                    playButtonText = playButtonText,
                    showMetadata = false,
                    showDescription = true,
                )
            }
            HorizontalDivider()
        }
    }
}
