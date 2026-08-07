package io.github.posaydone.kinopub.mobile

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.pip.PictureInPictureDelegate
import androidx.core.pip.VideoPlaybackPictureInPicture
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import io.github.posaydone.kinopub.core.model.AuthEvent
import io.github.posaydone.kinopub.core.model.SessionManager
import io.github.posaydone.kinopub.mobile.navigation.RootGraph
import io.github.posaydone.kinopub.mobile.ui.theme.KinopubTheme
import io.github.posaydone.kinopub.mobile.ui.utils.LocalPipController
import io.github.posaydone.kinopub.mobile.ui.utils.PipController
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.Executors
import javax.inject.Inject

private val DEFAULT_PIP_ASPECT_RATIO = Rational(16, 9)

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PipController {

    @Inject
    lateinit var sessionManager: SessionManager // Inject SessionManager

    @Inject
    @JvmSuppressWildcards
    lateinit var authEventFlow: SharedFlow<AuthEvent> // Inject the flow

    // Framework calls triggered by setPictureInPictureParams/enterPictureInPictureMode are
    // offloaded to a background executor, as recommended by the androidx.core:core-pip docs.
    private val pipExecutor = Executors.newSingleThreadExecutor()
    private val pip = VideoPlaybackPictureInPicture(this, pipExecutor)

    private var isInPipModeState by mutableStateOf(false)
    override val isInPip: Boolean get() = isInPipModeState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        pip.addOnPictureInPictureEventListener(
            ContextCompat.getMainExecutor(this),
            object : PictureInPictureDelegate.OnPictureInPictureEventListener {
                override fun onPictureInPictureEvent(
                    event: PictureInPictureDelegate.Event,
                    config: Configuration?,
                ) {
                    when (event) {
                        PictureInPictureDelegate.Event.ENTERED -> {
                            isInPipModeState = true
                            // Let the system freely resize/rotate the PiP window instead of
                            // holding it to the player's locked landscape orientation.
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }

                        PictureInPictureDelegate.Event.EXITED -> {
                            isInPipModeState = false
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }

                        else -> Unit
                    }
                }
            },
        )

        setContent {
            CompositionLocalProvider(LocalPipController provides this) {
                KinopubTheme {
                    RootGraph(
                        sessionManager = sessionManager,
                        authEventFlow = authEventFlow
                    )
                }
            }
        }
    }

    override fun updatePictureInPicture(enabled: Boolean, playerView: View?, aspectRatio: Rational?) {
        pip.setEnabled(enabled)
        pip.setAspectRatio(aspectRatio ?: DEFAULT_PIP_ASPECT_RATIO)
        pip.setPlayerView(playerView)
        pip.commit()
    }

    override fun onDestroy() {
        pip.close()
        pipExecutor.shutdown()
        super.onDestroy()
    }
}
