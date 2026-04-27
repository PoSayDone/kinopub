package io.github.posaydone.filmix.mobile.ui.screen.authScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRightAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.posaydone.filmix.core.common.R
import io.github.posaydone.filmix.core.common.sharedViewModel.AuthScreenUiState
import io.github.posaydone.filmix.core.common.sharedViewModel.AuthScreenViewModel

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

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.ic_kinopub),
                contentDescription = stringResource(R.string.kinopub_icon),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.device_sign_in_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.device_sign_in_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            if (uiState is AuthScreenUiState.AwaitingActivation) {
                val activation = uiState as AuthScreenUiState.AwaitingActivation

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = activation.userCode,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activation.verificationUri,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.device_sign_in_waiting),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.authorizeUser() },
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
                enabled = uiState != AuthScreenUiState.Loading
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState as AuthScreenUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
