package io.github.posaydone.kinopub.tv.ui.utils

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.BringIntoViewSpec
import kotlin.math.abs

class CustomBringIntoViewSpec(
    private val parentFraction: Float,
    private val childFraction: Float,
) : BringIntoViewSpec {

    override val scrollAnimationSpec: AnimationSpec<Float> =
        tween(durationMillis = 50, easing = EaseInOut)

    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
        val leadingEdgeOfItemRequestingFocus = offset
        val trailingEdgeOfItemRequestingFocus = offset + size
        
        val sizeOfItemRequestingFocus =
            abs(trailingEdgeOfItemRequestingFocus - leadingEdgeOfItemRequestingFocus)
        val childSmallerThanParent = sizeOfItemRequestingFocus <= containerSize
        val initialTargetForLeadingEdge =
            (parentFraction * containerSize) - (childFraction * sizeOfItemRequestingFocus)
        val spaceAvailableToShowItem = containerSize - initialTargetForLeadingEdge

        val targetForLeadingEdge =
            if (childSmallerThanParent && spaceAvailableToShowItem < sizeOfItemRequestingFocus) {
                containerSize - sizeOfItemRequestingFocus
            } else {
                initialTargetForLeadingEdge
            }

        return leadingEdgeOfItemRequestingFocus - targetForLeadingEdge
    }
}
