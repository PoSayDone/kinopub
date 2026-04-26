package io.github.posaydone.filmix.tv.ui.common

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding
import io.github.posaydone.filmix.tv.ui.utils.CustomBringIntoViewSpec

enum class ItemDirection(val aspectRatio: Float) {
    Vertical(10.5f / 16f), Horizontal(16f / 9f);
}

private const val TAG = "ShowsRow"

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun ShowsRow(
    cardWidth: Dp = 118.dp,
    showList: ShowList,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Medium, fontSize = 16.sp
    ),
    showItemTitle: Boolean = true,
    showIndexOverImage: Boolean = false,
    onShowSelected: (show: Show) -> Unit = {},
    onShowFocused: ((Show) -> Unit)? = {},
    onViewAll: (() -> Unit)? = null,
) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }
    val horizontalBivs = remember { CustomBringIntoViewSpec(0.4f, 0f) }

    CompositionLocalProvider(LocalBringIntoViewSpec provides horizontalBivs) {
        Column(
            modifier = modifier
                .focusable(false)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = titleStyle,
                    modifier = Modifier
                        .alpha(1.0f)
                        .padding(start = startPadding, top = 16.dp, bottom = 16.dp)
                )
            }
            AnimatedContent(
                targetState = showList,
                label = "",
            ) {
                LazyRow(
                    contentPadding = PaddingValues(
                        start = startPadding,
                        end = endPadding,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .focusRequester(lazyRow)
                        .focusRestorer(firstItem)
                ) {
                    if (onViewAll != null) {
                        item {
                            BaseCard(
                                onClick = { onViewAll() },
                                modifier = Modifier
                                    .width(cardWidth)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .aspectRatio(ItemDirection.Vertical.aspectRatio),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "View All",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(showList, key = { _, show -> show.id }) { index, show ->
                        val itemModifier = if (index == 0) {
                            Modifier.focusRequester(firstItem)
                        } else {
                            Modifier
                        }

                        ShowsRowItem(
                            modifier = itemModifier.weight(1f),
                            index = index,
                            cardWidth = cardWidth,
                            itemDirection = itemDirection,
                            onShowSelected = {
                                lazyRow.saveFocusedChild()
                                onShowSelected(it)
                            },
                            onShowFocused = { onShowFocused?.invoke(show) },
                            show = show,
                            showItemTitle = showItemTitle,
                            showIndexOverImage = showIndexOverImage
                        )
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ShowsRowItem(
    index: Int,
    show: Show,
    onShowSelected: (Show) -> Unit,
    showItemTitle: Boolean,
    showIndexOverImage: Boolean,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    onShowFocused: (Show) -> Unit = {},
    cardWidth: Dp = 148.dp,
) {
    var isFocused by remember { mutableStateOf(false) }

    BaseCard(
        onClick = { onShowSelected(show) },
        modifier = Modifier
            .width(cardWidth)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onShowFocused(show)
                }
            }
            .then(modifier),
        title = {
            ShowsRowItemText(
                showItemTitle = showItemTitle,
                isItemFocused = isFocused,
                title = show.title.substringBefore('/').trim(),
            )
        },
    ) {
        ShowsRowItemImage(
            modifier = Modifier.aspectRatio(itemDirection.aspectRatio),
            showIndexOverImage = showIndexOverImage,
            show = show,
            index = index
        )
    }
}

@Composable
private fun ShowsRowItemImage(
    show: Show,
    showIndexOverImage: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = Modifier, contentAlignment = Alignment.CenterStart) {
        PosterImage(
            contentDescritpion = show.title,
            imageUrl = show.poster,
            modifier = modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    if (showIndexOverImage) {
                        drawRect(
                            color = Color.Black.copy(
                                alpha = 0.1f
                            )
                        )
                    }
                },
        )
        if (showIndexOverImage) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "#${index.inc()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = Shadow(
                        offset = Offset(0.5f, 0.5f), blurRadius = 5f
                    ), color = Color.White
                ),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
internal fun ShowsRowItemText(
    showItemTitle: Boolean,
    isItemFocused: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    titleMode: CardTitleMode = CardTitleMode.ON_FOCUS,
) {
    if (showItemTitle) {
        val alpha by animateFloatAsState(
            targetValue = if (titleMode == CardTitleMode.ALWAYS || isItemFocused) 1f else 0f,
            label = "",
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = textAlign,
            modifier = modifier
                .alpha(alpha)
                .padding(top = 16.dp),
            softWrap = true,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
