package io.github.posaydone.kinopub.tv.ui.screen.profileScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.tv.ui.common.StandardDialog
import io.github.posaydone.kinopub.tv.ui.common.TextField

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalTvMaterial3Api::class,
)
@Composable
internal fun ApiUrlDialog(
    showDialog: Boolean,
    currentUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onReset: () -> Unit,
) {
    var url by rememberSaveable(currentUrl) { mutableStateOf(currentUrl) }

    StandardDialog(
        showDialog = showDialog,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.api_url_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.api_url_dialog_description))
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholderText = "https://cdn-service.space/",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(url) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onReset) {
                Text(stringResource(R.string.api_url_reset))
            }
        },
    )
}
