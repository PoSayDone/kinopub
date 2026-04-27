package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.model.HistoryShow

@Composable
fun HistoryShowsRow(
    historyList: List<HistoryShow>,
    modifier: Modifier = Modifier,
    title: String,
    showItemOriginalTitle: Boolean = true,
    showItemYear: Boolean = true,
    onShowClick: (HistoryShow) -> Unit = { },
    onViewAll: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
    ) {
        if (onViewAll != null) {
            Surface(
                onClick = onViewAll,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HistoryShowsRowHeader(title = title, showViewAll = true)
            }
        } else {
            HistoryShowsRowHeader(title = title, showViewAll = false)
        }

        AnimatedContent(
            targetState = historyList,
            label = "",
        ) { historyList ->
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(
                    historyList,
                    key = { index, show -> "${show.id}_${show.seasonNumber}_${show.episodeNumber}_$index" },
                ) { _, show ->
                    HistoryCard(
                        show = show,
                        showOriginalTitle = showItemOriginalTitle,
                        showYear = showItemYear,
                        onClick = { onShowClick(show) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryShowsRowHeader(
    title: String,
    showViewAll: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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

        if (showViewAll) {
            Text(
                text = stringResource(R.string.view_all),
                style = MaterialTheme.typography.titleSmall.copy(
                    letterSpacing = 0.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
