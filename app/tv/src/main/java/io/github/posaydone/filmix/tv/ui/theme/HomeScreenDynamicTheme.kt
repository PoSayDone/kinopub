package io.github.posaydone.filmix.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme as ComposeColorScheme
import androidx.tv.material3.ColorScheme as TvColorScheme
import androidx.tv.material3.MaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun HomeScreenDynamicTheme(
    seedColor: Color?,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val baseColorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    val colorScheme = if (enabled && seedColor != null) {
        val dynamicColorScheme = rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = true,
        )

        remember(dynamicColorScheme, baseColorScheme) {
            dynamicColorScheme.toTvColorScheme(baseColorScheme)
        }
    } else {
        baseColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography,
        content = content,
    )
}

private fun ComposeColorScheme.toTvColorScheme(baseColorScheme: TvColorScheme): TvColorScheme =
    baseColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        border = outline,
        borderVariant = outlineVariant,
        scrim = scrim,
    )
