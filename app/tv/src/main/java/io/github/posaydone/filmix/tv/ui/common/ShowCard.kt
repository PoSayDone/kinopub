package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.tv.ui.theme.FilmixBorderWidth

enum class CardTitleMode {
    ON_FOCUS,
    ALWAYS,
}

@Composable
fun BaseCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    image: @Composable BoxScope.() -> Unit,
) {
    StandardCardContainer(
        modifier = modifier,
        title = title,
        imageCard = {
            Surface(
                onClick = onClick,
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(
                            width = FilmixBorderWidth,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                content = image,
            )
        },
    )
}

@Composable
fun ShowCard(
    show: Show,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleMode: CardTitleMode = CardTitleMode.ALWAYS,
    showTitle: Boolean = true,
    showOriginalTitle: Boolean = true,
    showYear: Boolean = true,
    badge: (@Composable BoxScope.() -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }

    BaseCard(
        onClick = onClick,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        title = {
            when (titleMode) {
                CardTitleMode.ALWAYS -> ShowCardInfo(
                    show = show,
                    showTitle = showTitle,
                    showOriginalTitle = showOriginalTitle,
                    showYear = showYear,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
                CardTitleMode.ON_FOCUS -> ShowsRowItemText(
                    showItemTitle = showTitle,
                    isItemFocused = isFocused,
                    title = show.title.substringBefore('/').trim(),
                    titleMode = titleMode,
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
        ) {
            PosterImage(
                imageUrl = show.poster,
                contentDescritpion = show.title,
                modifier = Modifier.fillMaxSize(),
            )
            badge?.invoke(this)
        }
    }
}

@Composable
fun ShowCardInfo(
    show: Show,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    showOriginalTitle: Boolean = true,
    showYear: Boolean = true,
) {
    if (!showTitle && !showOriginalTitle && !showYear) return

    val primaryTitle = show.title.substringBefore('/').trim()
    val originalTitle = show.title.substringAfter('/', missingDelimiterValue = "")
        .trim()
        .takeIf { it.isNotEmpty() }
        ?: show.originalTitle
            .trim()
            .takeIf { it.isNotEmpty() && !it.equals(primaryTitle, ignoreCase = true) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showTitle) {
            Text(
                text = primaryTitle,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showOriginalTitle && originalTitle != null) {
            Text(
                text = originalTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showYear && show.year > 0) {
            Text(
                text = show.year.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun ShowCardBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

fun Show.primaryRating(): Double? = ratingKp ?: ratingImdb
