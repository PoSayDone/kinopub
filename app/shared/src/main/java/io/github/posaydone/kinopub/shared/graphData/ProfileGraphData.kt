package io.github.posaydone.kinopub.shared.graphData

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey

@Serializable
sealed class ProfileGraphData : NavKey {
    @Serializable
    data object Profile : ProfileGraphData()

    @Serializable
    data object VideoQuality : ProfileGraphData()

    @Serializable
    data object StreamType : ProfileGraphData()
    
    @Serializable
    data object ServerLocation : ProfileGraphData()
}