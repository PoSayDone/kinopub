package io.github.posaydone.kinopub.mobile.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.posaydone.kinopub.core.common.R

@Composable
fun Error(
    modifier: Modifier = Modifier, onRetry: () -> Unit,
    children: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp, alignment = Alignment.CenterVertically
        ),
    ) {
        Text(
            text = stringResource(R.string.error_title), style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = stringResource(R.string.error_message),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onRetry,
        ) {
            Text(stringResource(R.string.retry))
        }
        children?.invoke()
    }
}
