package io.github.posaydone.filmix.tv.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver


class FocusRequesterModifiers private constructor(
    val parentModifier: Modifier,
    val parentFocusRequester: FocusRequester,
    val childModifier: Modifier,
    val childFocusRequester: FocusRequester,
    val needsRestore: MutableState<Boolean>
) {
    @OptIn(ExperimentalComposeUiApi::class)
    fun onNavigateOut() {
        needsRestore.value = true
        parentFocusRequester.saveFocusedChild()
    }

    companion object {
        /**
         * Returns a set of modifiers [FocusRequesterModifiers] which can be used for restoring focus and
         * specifying the initially focused item.
         */
        @OptIn(ExperimentalComposeUiApi::class)
        @Composable
        fun create(
            parentFocusRequester: FocusRequester = FocusRequester(),
            onComposableFocusEntered: (() -> Unit)? = null,
            onComposableFocusExited: (() -> Unit)? = null
        ): FocusRequesterModifiers {
            val focusRequester = remember { parentFocusRequester }
            val childFocusRequester = remember { FocusRequester() }
            val needsRestore = rememberSaveable { mutableStateOf(false) }

            val parentModifier = Modifier
                .focusRequester(focusRequester)
                .focusProperties {
                    exit = {
                        onComposableFocusExited?.invoke()
                        focusRequester.saveFocusedChild()
                        FocusRequester.Default
                    }
                    enter = {
                        onComposableFocusEntered?.invoke()
                        if (focusRequester.restoreFocusedChild()) { FocusRequester.Cancel }
                        else { childFocusRequester }
                    }
                }

            val childModifier = Modifier.focusRequester(childFocusRequester)

            LifecycleEventObserver { owner, event  ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    if (needsRestore.value) {
                        childFocusRequester.requestFocus()
                        needsRestore.value = false
                    }
                }
            }

            return FocusRequesterModifiers(
                parentModifier,
                focusRequester,
                childModifier,
                childFocusRequester,
                needsRestore
            )
        }
    }
}
