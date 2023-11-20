package sp.ax.jc.clicks

import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

@Composable
internal fun getLastPressState(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
): MutableState<PressInteraction.Press?> {
    val lastPressState = remember { mutableStateOf<PressInteraction.Press?>(null) }
    LaunchedEffect(lastPressState.value, enabled) {
        val lastPress = lastPressState.value
        if (lastPress != null && !enabled) {
            interactionSource.emit(PressInteraction.Cancel(lastPress))
            lastPressState.value = null
        }
    }
    return lastPressState
}


internal suspend fun PressGestureScope.onPress(
    offset: Offset,
    lastPressState: MutableState<PressInteraction.Press?>,
    interactionSource: MutableInteractionSource,
) {
    val press = PressInteraction.Press(offset)
    lastPressState.value = press
    interactionSource.emit(press)
    if (tryAwaitRelease()) {
        interactionSource.emit(PressInteraction.Release(press))
    } else {
        interactionSource.emit(PressInteraction.Cancel(press))
    }
    lastPressState.value = null
}
