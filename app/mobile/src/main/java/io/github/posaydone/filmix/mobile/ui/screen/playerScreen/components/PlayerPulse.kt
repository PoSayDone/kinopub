/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.posaydone.filmix.mobile.ui.screen.playerScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.seconds

object PlayerPulse {
    enum class Type { FORWARD, BACK, NONE }
}

@Composable
fun PlayerPulse(
    state: PlayerPulseState = rememberPlayerPulseState(),
) {
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.type == PlayerPulse.Type.BACK) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(88.dp)
                        .wrapContentSize()
                        .size(48.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.type == PlayerPulse.Type.FORWARD) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(88.dp)
                        .wrapContentSize()
                        .size(48.dp)
                )
            }
        }
    }
}

class PlayerPulseState {
    private var _type by mutableStateOf(PlayerPulse.Type.NONE)
    val type: PlayerPulse.Type get() = _type

    private val channel = Channel<Unit>(Channel.CONFLATED)

    @OptIn(FlowPreview::class)
    suspend fun observe() {
        channel.consumeAsFlow()
            .debounce(2.seconds)
            .collect { _type = PlayerPulse.Type.NONE }
    }

    fun setType(type: PlayerPulse.Type) {
        _type = type
        channel.trySend(Unit)
    }
}

@Composable
fun rememberPlayerPulseState() =
    remember { PlayerPulseState() }.also { LaunchedEffect(it) { it.observe() } }
