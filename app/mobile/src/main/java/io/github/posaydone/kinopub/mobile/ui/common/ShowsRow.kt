package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.Show
import io.github.posaydone.kinopub.core.model.ShowList

@Composable
fun ShowsRow(
    showList: ShowList,
    modifier: Modifier = Modifier,
    title: String,
    onShowClick: (Show) -> Unit = { },
    onViewAll: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
    ) {
        Surface(
            onClick = onViewAll ?: {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp, letterSpacing = 0.sp, fontWeight = FontWeight.SemiBold
                    ),
                )

                if (onViewAll != null) Text(
                    text = stringResource(R.string.view_all), style = MaterialTheme.typography.titleSmall.copy(
                        letterSpacing = 0.sp, fontWeight = FontWeight.SemiBold
                    ), color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedContent(
            targetState = showList,
            label = "",
        ) { showList ->
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                itemsIndexed(showList, key = { _, show -> show.id }) { _, show ->
                    ShowsRowItem(
                        modifier = modifier.weight(1f),
                        onMovieSelected = {
                            onShowClick(show)
                        },
                        show = show,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ShowsRowItem(
    show: Show,
    onMovieSelected: (Show) -> Unit,
    modifier: Modifier = Modifier,
) {

    ShowCard(
        show = show, onClick = { onMovieSelected(show) }, modifier = Modifier.then(modifier)
    )
}