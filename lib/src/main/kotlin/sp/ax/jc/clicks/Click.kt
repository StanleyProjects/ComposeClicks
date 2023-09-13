package sp.ax.jc.clicks

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo

/**
 * Configure component to receive clicks via tap gestures.
 * @see [Modifier.clickable]
 * @see [Modifier.indication]
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.2.2
 */
fun Modifier.onClick(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource,
    indication: Indication,
    block: () -> Unit,
): Modifier {
    return composed(
        inspectorInfo = debugInspectorInfo {
            name = "onClick"
            properties["enabled"] = enabled
            properties["block"] = block
            properties["indication"] = indication
            properties["interactionSource"] = interactionSource
        },
        factory = {
            val onClickState = rememberUpdatedState(block)
            Modifier.indication(interactionSource = interactionSource, indication = indication)
                .pointerInput(interactionSource, enabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (enabled) {
                                val press = PressInteraction.Press(offset)
                                interactionSource.emit(press)
                                @Suppress("IgnoredReturnValue")
                                tryAwaitRelease()
                                interactionSource.emit(PressInteraction.Release(press))
                            }
                        },
                        onTap = {
                            if (enabled) onClickState.value()
                        },
                    )
                }
        },
    )
}

/**
 * Configure component to receive clicks via tap gestures with default [MutableInteractionSource] and [Indication].
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.2.2
 */
fun Modifier.onClick(
    enabled: Boolean = true,
    block: () -> Unit,
): Modifier {
    return composed {
        Modifier.onClick(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current,
            block = block,
        )
    }
}
