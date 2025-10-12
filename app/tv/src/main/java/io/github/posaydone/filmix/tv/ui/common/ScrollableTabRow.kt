package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScrollableTabRow(
    items: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    var currentIndex by remember { mutableIntStateOf(selectedTabIndex) }
    val lazyListState = rememberLazyListState()
    val (lazyRow, firstItem) = remember { FocusRequester.Companion.createRefs() }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex < items.size) {
            lazyListState.animateScrollToItem(selectedTabIndex)
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(lazyRow)
            .focusRestorer(firstItem)
            .focusGroup(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { index ->
            Tab(
                modifier = Modifier.let {
                    if (index == 0) {
                        it.focusRequester(firstItem)
                    } else {
                        it
                    }
                },
                text = items[index],
                selected = index == currentIndex,
                onFocus = {
                    currentIndex = index
                    onTabSelected(index)
                },
                onClick = {
                    currentIndex = index
                    onTabSelected(index)
                }
            )
        }
    }
}

@Composable
fun Tab(
    text: String,
    selected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        modifier = modifier
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (isFocused) {
                    onFocus()
                }
            }, onClick = onClick, colors = when (selected) {
            true -> {
                ButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                )
            }

            false -> {
                ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                )
            }
        }

    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}