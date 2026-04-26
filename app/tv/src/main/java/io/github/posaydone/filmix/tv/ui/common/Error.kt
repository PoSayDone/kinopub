package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun Error(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

    Box(
        modifier = modifier
            .padding(start = 80.dp)
    ) {

        LazyColumn(
            modifier = Modifier
                .focusRequester(lazyRow)
                .focusRestorer(firstItem),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(
                space = 12.dp, alignment = Alignment.CenterVertically
            ),
        ) {
            item {
                Text(
                    text = "Error", style = MaterialTheme.typography.headlineLarge,
                )
            }
            item {
                Text(
                    text = "Filmix service is unavailable or you don't have internet connection. Try again later.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            item {
                Button(
                    modifier = Modifier
                        .focusRequester(firstItem),
                    onClick = onRetry,
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
