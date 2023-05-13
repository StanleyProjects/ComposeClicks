package sp.ax.jc.clicks

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput

// todo pointerInput key
/**
 * Configure component to receive clicks via tap gestures.
 * @see [Modifier.clickable]
 * @see [Modifier.indication]
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.1.0-3
 */
fun Modifier.onClick(
    key1: Any?,
    interactionSource: MutableInteractionSource,
    indication: Indication,
    block: () -> Unit,
): Modifier {
    return indication(interactionSource = interactionSource, indication = indication)
        .pointerInput(key1) {
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    interactionSource.emit(press)
                    @Suppress("IgnoredReturnValue")
                    tryAwaitRelease()
                    interactionSource.emit(PressInteraction.Release(press))
                },
                onTap = {
                    block()
                },
            )
        }
}

/**
 * Configure component to receive clicks via tap gestures with default [MutableInteractionSource] and [Indication].
 * @see [Modifier.clickable]
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.1.0-3
 */
fun Modifier.onClick(block: () -> Unit): Modifier {
    return composed {
        Modifier.onClick(
            key1 = null,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current,
            block = block,
        )
    }
}
