package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.model.HistoryShow
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding
import io.github.posaydone.filmix.tv.ui.utils.CustomBringIntoViewSpec

private val CardWidth = 220.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryShowsRow(
    historyList: List<HistoryShow>,
    title: String,
    modifier: Modifier = Modifier,
    showItemTitle: Boolean = true,
    showItemOriginalTitle: Boolean = true,
    showItemYear: Boolean = true,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    onShowSelected: (HistoryShow) -> Unit = {},
    onShowFocused: ((HistoryShow) -> Unit)? = null,
    onViewAll: (() -> Unit)? = null,
) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }
    val horizontalBivs = remember { CustomBringIntoViewSpec(0.4f, 0f) }

    CompositionLocalProvider(LocalBringIntoViewSpec provides horizontalBivs) {
        androidx.compose.foundation.layout.Column(modifier = modifier.focusable(false)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                ),
                modifier = Modifier.padding(start = startPadding, top = 16.dp, bottom = 16.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(start = startPadding, end = endPadding),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer(firstItem),
            ) {
                if (onViewAll != null) {
                    item {
                        BaseCard(
                            onClick = onViewAll,
                            modifier = Modifier
                                .width(CardWidth)
                                .focusRequester(firstItem),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
                itemsIndexed(
                    historyList,
                    key = { index, show -> "${show.id}_${show.seasonNumber}_${show.episodeNumber}_$index" },
                ) { index, show ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }

                    HistoryShowCard(
                        show = show,
                        showTitle = showItemTitle,
                        showOriginalTitle = showItemOriginalTitle,
                        showYear = showItemYear,
                        onClick = {
                            lazyRow.saveFocusedChild()
                            onShowSelected(show)
                        },
                        modifier = itemModifier
                            .width(CardWidth)
                            .onFocusChanged { if (it.isFocused) onShowFocused?.invoke(show) },
                    )
                }
            }
        }
    }
}


