package sp.ax.jc.clicks

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo

/**
 * Configure component to receive clicks via tap and press gestures.
 * @see [Modifier.indication]
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.2.3
 */
fun Modifier.clicks(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource,
    indication: Indication,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier {
    return composed(
        inspectorInfo = debugInspectorInfo {
            name = "clicks"
            properties["enabled"] = enabled
            properties["onClick"] = onClick
            properties["onLongClick"] = onLongClick
            properties["indication"] = indication
            properties["interactionSource"] = interactionSource
        },
        factory = {
            val onClickState = rememberUpdatedState(onClick)
            val onLongClickState = rememberUpdatedState(onLongClick)
            val lastPressState = getLastPressState(
                enabled = enabled,
                interactionSource = interactionSource,
            )
            Modifier
                .indication(interactionSource = interactionSource, indication = indication)
                .pointerInput(interactionSource, enabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (enabled) onPress(
                                offset = offset,
                                lastPressState = lastPressState,
                                interactionSource = interactionSource,
                            )
                        },
                        onLongPress = {
                            if (enabled) onLongClickState.value()
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
 * Configure component to receive clicks via tap and press gestures with default [MutableInteractionSource] and [Indication].
 * @author [Stanley Wintergreen](https://github.com/kepocnhh)
 * @since 0.2.3
 */
fun Modifier.clicks(
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier {
    return composed {
        Modifier.clicks(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}
