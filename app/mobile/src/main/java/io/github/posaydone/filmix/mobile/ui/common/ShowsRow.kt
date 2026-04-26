package io.github.posaydone.filmix.mobile.ui.common

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList

@Composable
fun ShowsRow(
    showList: ShowList,
    modifier: Modifier = Modifier,
    title: String,
    onShowClick: (Show) -> Unit = { },
    onViewAll: (() -> Unit)? = null,
) {

    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

    Column(
        modifier = modifier.focusGroup()
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
                        fontSize = 18.sp,
                        letterSpacing = 0.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                )

                if (onViewAll != null)
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.titleSmall.copy(
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
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
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRestorer(firstItem)
            ) {

                itemsIndexed(showList, key = { _, show -> show.id }) { index, show ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                    ShowsRowItem(
                        modifier = itemModifier.weight(1f),
                        index = index,
                        onMovieSelected = {
                            lazyRow.saveFocusedChild()
                            onShowClick(show)
                        },
                        onMovieFocused = {},
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
    index: Int,
    show: Show,
    onMovieSelected: (Show) -> Unit,
    modifier: Modifier = Modifier,
    onMovieFocused: (Show) -> Unit = {},
) {

    ShowCard(
        show = show, onClick = { onMovieSelected(show) }, modifier = Modifier
            .onFocusChanged {
                if (it.isFocused) {
                    onMovieFocused(show)
                }
            }
            .then(modifier))
}