package io.github.posaydone.kinopub.tv.ui.utils

import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.ui.geometry.Rect

class ImmersiveShowRowViewResponder(
    private val bivsParentFraction: Float,
    private val bivsChildFraction: Float,
    private val parentFractionToAchieve: Float,
    private val childFractionToAchieve: Float,
    private val viewportHeightPx: Int,
) : BringIntoViewResponder {

    override suspend fun bringChildIntoView(localRect: () -> Rect?) {
        // do nothing
    }

    override fun calculateRectForParent(localRect: Rect): Rect {
        val bivsAlignmentLine =
            viewportHeightPx * bivsParentFraction - localRect.height * bivsChildFraction
        val aligmentLineToAchieve =
            viewportHeightPx * parentFractionToAchieve - localRect.height * childFractionToAchieve
        val diff = bivsAlignmentLine - aligmentLineToAchieve
        return localRect.translate(translateX = 0f, translateY = diff)
    }
}
