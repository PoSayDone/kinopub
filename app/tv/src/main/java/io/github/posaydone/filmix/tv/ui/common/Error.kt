package io.github.posaydone.filmix.tv.ui.common

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.filmix.core.common.R

@Composable
fun Error(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

    Column(
        modifier = modifier
            .padding(start = 80.dp)
            .focusable(false),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp, alignment = Alignment.CenterVertically
        ),
    ) {
        Text(
            text = stringResource(R.string.error_title), style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = stringResource(R.string.error_message),
            style = MaterialTheme.typography.bodyLarge,
        )

        LazyRow(
            modifier = Modifier
                .focusRequester(lazyRow)
                .focusRestorer(firstItem),
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                Button(
                    modifier = Modifier
                        .focusRequester(firstItem),
                    onClick = onRetry,
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}
