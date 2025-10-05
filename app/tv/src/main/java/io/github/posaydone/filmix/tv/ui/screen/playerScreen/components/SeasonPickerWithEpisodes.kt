package io.github.posaydone.filmix.tv.ui.screen.playerScreen.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
) {
    var currentIndex by remember { mutableIntStateOf(selectedTabIndex) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .focusRestorer()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .focusGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEachIndexed { index, text ->
                key(index) {
                    Tab(text = text, selected = index == currentIndex, onFocus = {
                        currentIndex = index
                        onTabSelected(index)
                    }, onClick = {
                        currentIndex = index
                        onTabSelected(index)
                    })
                }
            }
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
