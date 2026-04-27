package io.github.posaydone.kinopub.tv.ui.common

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp

@Composable
fun Loading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.focusable().padding(start = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = 0.75f,
            modifier = Modifier.size(64.dp),
        )
    }
}