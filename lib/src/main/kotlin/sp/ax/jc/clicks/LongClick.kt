package sp.ax.jc.clicks

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo

/**
 * Configure component to receive clicks via press gestures.
 * @see [Modifier.indication]
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.2.3
 */
fun Modifier.onLongClick(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource,
    indication: Indication,
    block: () -> Unit,
): Modifier {
    return composed(
        inspectorInfo = debugInspectorInfo {
            name = "onLongClick"
            properties["enabled"] = enabled
            properties["block"] = block
            properties["indication"] = indication
            properties["interactionSource"] = interactionSource
        },
        factory = {
            val onLongClickState = rememberUpdatedState(block)
            val lastPressState = getLastPressState(
                enabled = enabled,
                interactionSource = interactionSource,
            )
            Modifier.indication(interactionSource = interactionSource, indication = indication)
                .pointerInput(interactionSource, enabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (enabled) {
                                onPress(
                                    offset = offset,
                                    lastPressState = lastPressState,
                                    interactionSource = interactionSource,
                                )
                            }
                        },
                        onLongPress = {
                            if (enabled) onLongClickState.value()
                        },
                    )
                }
        },
    )
}

/**
 * Configure component to receive clicks via press gestures with default [MutableInteractionSource] and [Indication].
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.2.3
 */
fun Modifier.onLongClick(
    enabled: Boolean = true,
    block: () -> Unit,
): Modifier {
    return composed {
        Modifier.onLongClick(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current,
            block = block,
        )
    }
}
