package io.github.posaydone.kinopub.tv.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.relocation.bringIntoView

internal fun Modifier.bringIntoViewIfChildrenAreFocused(): Modifier {
    return this then BringIntoViewElement()
}

private class BringIntoViewElement : ModifierNodeElement<BringIntoViewModifier>() {
    override fun create(): BringIntoViewModifier {
        return BringIntoViewModifier()
    }

    override fun equals(other: Any?): Boolean {
        return other is BringIntoViewElement
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }

    override fun update(node: BringIntoViewModifier) {
    }
}

private class BringIntoViewModifier : Modifier.Node(), BringIntoViewModifierNode {
    override suspend fun bringIntoView(
        childCoordinates: LayoutCoordinates,
        boundsProvider: () -> Rect?,
    ) {
        boundsProvider()?.let { bounds ->
            this.node.bringIntoView { bounds }
        }
    }

}