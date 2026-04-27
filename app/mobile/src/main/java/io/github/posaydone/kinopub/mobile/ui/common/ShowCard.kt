package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.kinopub.core.model.Show

private val ShowCardWidth = 140.dp
private val ShowCardInfoTopPadding = 4.dp

@Composable
fun BaseCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    image: @Composable BoxScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                disabledContentColor = Color(android.graphics.Color.TRANSPARENT),
                containerColor = Color(android.graphics.Color.TRANSPARENT),
            ),
            onClick = onClick,
        ) {
            Box(content = image)
        }
        title()
    }
}

@Composable
fun ShowCardInfo(
    title: String,
    originalTitle: String? = null,
    year: Int? = null,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    showOriginalTitle: Boolean = true,
    showYear: Boolean = true,
) {
    val primaryTitle = title.trim()
    val secondaryTitle = originalTitle
        ?.trim()
        ?.takeIf { it.isNotEmpty() && !it.equals(primaryTitle, ignoreCase = true) }
    val resolvedYear = year?.takeIf { it > 0 }

    if (!showTitle && !showOriginalTitle && !showYear) return
    if (!showTitle && secondaryTitle == null && resolvedYear == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ShowCardInfoTopPadding),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showTitle) {
            Text(
                text = primaryTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showOriginalTitle && secondaryTitle != null) {
            Text(
                text = secondaryTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showYear && resolvedYear != null) {
            Text(
                text = resolvedYear.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun ShowCard(
    show: Show,
    modifier: Modifier = Modifier,
    showOriginalTitle: Boolean = false,
    showYear: Boolean = false,
    onClick: () -> Unit
) {
    BaseCard(
        onClick = onClick,
        modifier = modifier.width(ShowCardWidth),
        title = {
            ShowCardInfo(
                title = show.title,
                originalTitle = show.originalTitle,
                year = show.year,
                showOriginalTitle = showOriginalTitle,
                showYear = showYear,
            )
        },
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .crossfade(true)
                .data(show.poster)
                .build(),
            contentDescription = show.title,
            modifier = Modifier
                .aspectRatio(2f / 3f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
    }
}
