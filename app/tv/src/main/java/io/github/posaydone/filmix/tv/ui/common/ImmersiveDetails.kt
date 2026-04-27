package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.posaydone.filmix.core.common.utils.formatDuration
import io.github.posaydone.filmix.core.common.utils.formatVoteCount
import io.github.posaydone.filmix.core.model.KinopoiskCountry
import io.github.posaydone.filmix.core.model.KinopoiskGenre
import io.github.posaydone.filmix.core.model.Rating
import io.github.posaydone.filmix.core.model.Votes
import kotlin.math.max

/**
 * A reusable composable that displays the main details of a movie or show,
 * including a logo or title, metadata, and description.
 */
@Composable
fun ImmersiveDetails(
    logoUrl: String?,
    title: String,
    description: String?,
    rating: Rating?,
    votes: Votes?,
    genres: List<KinopoiskGenre>?,
    countries: List<KinopoiskCountry>?,
    year: Int?,
    seriesLength: Int?,
    movieLength: Int?,
    ageRating: String,
    modifier: Modifier = Modifier.Companion,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current

    val slashIndex = title.indexOf('/')
    val primaryTitle = if (slashIndex != -1) title.substring(0, slashIndex).trim() else title
    val originalTitle = if (slashIndex != -1) title.substring(slashIndex + 1).trim().takeIf { it.isNotEmpty() } else null

    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(logoUrl).build(),
                    contentDescription = primaryTitle,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.sizeIn(
                        maxWidth = screenWidth * 0.4f, maxHeight = screenHeight * 0.16f
                    )
                )
            } else {
                Text(
                    text = primaryTitle,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium),
                )
            }
            if (originalTitle != null) {
                Text(
                    text = originalTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }

        MetadataRow(
            rating = rating,
            votes = votes,
            year = year,
            genres = genres,
            totalMinutes = max(movieLength ?: 0, seriesLength ?: 0),
            countries = countries,
            ageRating = ageRating
        )

        if (!description.isNullOrBlank()) {
            Text(
                modifier = Modifier.sizeIn(
                    maxWidth = 400.dp
                ), text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.sp, lineHeight = 20.sp
                ), maxLines = 3, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetadataRow(
    rating: Rating?,
    votes: Votes?,
    year: Int?,
    genres: List<KinopoiskGenre>?,
    totalMinutes: Int,
    countries: List<KinopoiskCountry>?,
    ageRating: String,
) {
    val context = LocalContext.current
    val metadataParts = remember(rating, votes, year, genres, totalMinutes, countries, ageRating) {
        buildList {
            val ratingText = rating?.kp?.let { "%.1f".format(it) }
            val votesText = votes?.kp?.let { formatVoteCount(it) }
            if (ratingText != null) {
                add(if (votesText != null) "$ratingText ($votesText)" else ratingText)
            }
            year?.let { add(it.toString()) }
            genres?.mapNotNull { it.name }?.take(2)?.joinToString(", ")
                ?.let { if (it.isNotEmpty()) add(it) }
            val durationText = formatDuration(context, totalMinutes)
            if (durationText.isNotEmpty()) add(durationText)
            countries?.mapNotNull { it.name }?.take(2)?.joinToString(", ")
                ?.let { if (it.isNotEmpty()) add(it) }
            if (ageRating.isNotEmpty()) add("$ageRating+")
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        metadataParts.forEachIndexed { index, part ->
            Text(
                text = part,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            if (index < metadataParts.lastIndex) {
                Text(
                    text = "•",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}