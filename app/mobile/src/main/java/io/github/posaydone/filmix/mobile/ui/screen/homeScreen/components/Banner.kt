package io.github.posaydone.filmix.mobile.ui.screen.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.mobile.ui.screen.showDetailsScreen.ActionButtons
import io.github.posaydone.filmix.mobile.ui.screen.showDetailsScreen.ShowPoster
import io.github.posaydone.filmix.mobile.ui.screen.showDetailsScreen.TitleSection
import io.github.posaydone.filmix.mobile.ui.utils.bottomBorder

/**
 * A reusable banner component for the home screen.
 *
 * @param show The show to display in the banner.
 * @param modifier Modifier for this component.
 * @param onClick Lambda to be invoked when the banner is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBanner(
    modifier: Modifier = Modifier,
    featuredShow: io.github.posaydone.filmix.core.model.FullShow,
    navigateToMoviePlayer: (showId: Int) -> Unit,
    onClick: (Int) -> Unit,
) {
    val headerHeight = 420.dp

    Box(modifier = modifier) {
        ShowPoster(
            backdropUrl = featuredShow.backdropUrl,
            posterUrl = featuredShow.posterUrl,
            height = headerHeight
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .bottomBorder(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Spacer(modifier = Modifier.height(headerHeight - 100.dp))

            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TitleSection(
                    title = featuredShow.title,
                    logoUrl = featuredShow.logoUrl,
                    height = 70.dp,
                )
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = featuredShow.description ?: featuredShow.shortDescription ?: "",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                    ActionButtons(
                        playButtonText = stringResource(R.string.continue_watching_banner),
                        navigateToMoviePlayer = { navigateToMoviePlayer(featuredShow.id) },
                    )
                }
            }
            HorizontalDivider()
        }
    }
}
