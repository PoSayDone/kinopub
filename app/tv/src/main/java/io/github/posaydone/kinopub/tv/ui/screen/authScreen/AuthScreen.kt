package io.github.posaydone.kinopub.tv.ui.screen.authScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.posaydone.kinopub.core.common.R
import io.github.posaydone.kinopub.core.common.sharedViewModel.AuthScreenUiState
import io.github.posaydone.kinopub.core.common.sharedViewModel.AuthScreenViewModel
import io.github.posaydone.kinopub.tv.ui.common.LargeButton
import io.github.posaydone.kinopub.tv.ui.common.LargeButtonStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRightAlt
import androidx.compose.ui.res.stringResource

@Composable
fun AuthScreen(
    navigateToHome: () -> Unit,
    viewModel: AuthScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AuthScreenUiState.Success) {
            navigateToHome()
            viewModel.onNavigationHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .fillMaxHeight()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.device_sign_in_title),
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.device_sign_in_description),
            style = MaterialTheme.typography.bodyLarge,
        )

        if (uiState is AuthScreenUiState.AwaitingActivation) {
            val activation = uiState as AuthScreenUiState.AwaitingActivation

            Spacer(Modifier.height(32.dp))
            Text(
                text = activation.userCode,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = activation.verificationUri,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.device_sign_in_waiting),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(24.dp))

        LargeButton(
            onClick = { viewModel.authorizeUser() },
            enabled = uiState != AuthScreenUiState.Loading,
            style = LargeButtonStyle.FILLED
        ) {
            Text(
                text = if (uiState is AuthScreenUiState.AwaitingActivation) {
                    stringResource(R.string.device_sign_in_refresh)
                } else {
                    stringResource(R.string.device_sign_in_start)
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.size(12.dp))
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.AutoMirrored.Rounded.ArrowRightAlt,
                contentDescription = null
            )
        }

        if (uiState is AuthScreenUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (uiState as AuthScreenUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
