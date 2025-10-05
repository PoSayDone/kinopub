package io.github.posaydone.filmix.tv.ui.common

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.bringIntoViewResponder
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.sharedViewModel.ImmersiveContentUiState
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import io.github.posaydone.filmix.tv.ui.screen.homeScreen.rememberChildPadding
import io.github.posaydone.filmix.tv.ui.utils.CustomBringIntoViewSpec
import io.github.posaydone.filmix.tv.ui.utils.ImmersiveShowRowViewResponder

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
                            ShowCard(
                                onClick = { onViewAll() },
                                modifier = Modifier
                                    .width(cardWidth)
                                    .focusRequester(firstItem),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImmersiveShowsRow(
    showList: ShowList,
    immersiveState: ImmersiveContentUiState,
    onShowFocused: (Show) -> Unit,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Normal, fontSize = 18.sp
    ),
    showItemTitle: Boolean = true,
    showIndexOverImage: Boolean = false,
    onShowSelected: (Show) -> Unit = {},
    requestInitialFocus: Boolean = false,
) {
    var isListFocused by remember { mutableStateOf(false) }
    var selectedShow by remember(showList) { mutableStateOf(showList.first()) }

    val screenHeight = LocalWindowInfo.current.containerSize.height

    Box(
        modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.run { screenHeightDp.dp } - 32.dp)) {
            AnimatedVisibility(
                visible = isListFocused, enter = fadeIn(), exit = fadeOut()
            ) {
                if (immersiveState is ImmersiveContentUiState.Content) {
                    ImmersiveBackground(
                        imageUrl = immersiveState.fullShow.backdropUrl
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .gradientOverlay(MaterialTheme.colorScheme.surface)
                    )

                    val childPadding = rememberChildPadding()
                    ImmersiveDetails(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = childPadding.start, top = childPadding.top + 24.dp)
                            .fillMaxWidth(),
                        logoUrl = immersiveState.fullShow.logoUrl,
                        title = immersiveState.fullShow.title,
                        description = immersiveState.fullShow.description
                            ?: immersiveState.fullShow.shortDescription,
                        rating = io.github.posaydone.filmix.core.model.Rating(
                            kp = immersiveState.fullShow.ratingKp ?: 0.0,
                            imdb = immersiveState.fullShow.ratingImdb ?: 0.0,
                            filmCritics = 0.0,
                            russianFilmCritics = 0.0,
                            await = 0.0
                        ),
                        votes = io.github.posaydone.filmix.core.model.Votes(
                            kp = immersiveState.fullShow.votesKp ?: 0,
                            imdb = immersiveState.fullShow.votesImdb ?: 0,
                            filmCritics = 0,
                            russianFilmCritics = 0,
                            await = 0
                        ),
                        genres = immersiveState.fullShow.genres.map {
                            io.github.posaydone.filmix.core.model.KinopoiskGenre(
                                name = it
                            )
                        },
                        countries = immersiveState.fullShow.countries.map {
                            io.github.posaydone.filmix.core.model.KinopoiskCountry(
                                name = it
                            )
                        },
                        year = immersiveState.fullShow.year,
                        seriesLength = immersiveState.fullShow.seriesLength,
                        movieLength = immersiveState.fullShow.movieLength,
                        ageRating = immersiveState.fullShow.ageRating.toString()
                    )
                } else if (immersiveState is ImmersiveContentUiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {}
                }
            }

            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .onFocusChanged { isListFocused = it.hasFocus }) {
                ShowsRow(
                    showList = showList,
                    itemDirection = itemDirection,
                    startPadding = startPadding,
                    endPadding = endPadding,
                    title = title,
                    titleStyle = titleStyle,
                    showItemTitle = showItemTitle,
                    showIndexOverImage = showIndexOverImage,
                    onShowSelected = onShowSelected,
                    onShowFocused = { it ->
                        onShowFocused(it)
                    },
                )
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

    ShowCard(
        onClick = { onShowSelected(show) }, title = {
            ShowsRowItemText(
                showItemTitle = showItemTitle, isItemFocused = isFocused, show = show
            )
        }, modifier = Modifier
            .width(cardWidth)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onShowFocused(show)
                }
            }
            .then(modifier)) {
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
private fun ShowsRowItemText(
    showItemTitle: Boolean,
    isItemFocused: Boolean,
    show: Show,
    modifier: Modifier = Modifier,
) {
    if (showItemTitle) {
        val movieNameAlpha by animateFloatAsState(
            targetValue = if (isItemFocused) 1f else 0f,
            label = "",
        )
        Text(
            text = show.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = modifier
                .alpha(movieNameAlpha)
                .padding(top = 16.dp),
            softWrap = true,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
