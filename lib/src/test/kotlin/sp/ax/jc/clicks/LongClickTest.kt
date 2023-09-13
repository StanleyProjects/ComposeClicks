package sp.ax.jc.clicks

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
internal class LongClickTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun onLongClickDefaultTest() {
        val value = AtomicBoolean(false)
        val tag = "onLongClick:default"
        rule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(tag)
                    .onLongClick(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                    ) {
                        value.set(!value.get())
                    },
            )
        }
        onLongClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onLongClickDefaultEnabledTest() {
        val value = AtomicBoolean(false)
        val tag = "onLongClick:enabled"
        rule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(tag)
                    .onLongClick(
                        enabled = true,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                    ) {
                        value.set(!value.get())
                    },
            )
        }
        onLongClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onLongClickDefaultDisabledTest() {
        val value = AtomicBoolean(false)
        val tag = "onLongClick:disabled"
        rule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(tag)
                    .onLongClick(
                        enabled = false,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                    ) {
                        value.set(!value.get())
                    },
            )
        }
        onLongClickDisabledAssert(value = value, tag = tag)
    }

    @Test
    fun onLongClickTest() {
        val value = AtomicBoolean(false)
        val tag = "onLongClick"
        rule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(tag)
                    .onLongClick {
                        value.set(!value.get())
                    },
            )
        }
        onLongClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onLongClickEnabledTest() {
        val value = AtomicBoolean(false)
        val tag = "onLongClick:enabled"
        rule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(tag)
                    .onLongClick(enabled = true) {
                        value.set(!value.get())
                    },
            )
        }
        onLongClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onLongClickDisabledTest() {
        val value = AtomicBoolean(false)
        val tag = "onLongClick:disabled"
        rule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(tag)
                    .onLongClick(enabled = false) {
                        value.set(!value.get())
                    },
            )
        }
        onLongClickDisabledAssert(value = value, tag = tag)
    }

    private fun onLongClickEnabledAssert(
        value: AtomicBoolean,
        tag: String,
    ) {
        assertFalse(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertTrue(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(value.get())
    }

    private fun onLongClickDisabledAssert(
        value: AtomicBoolean,
        tag: String,
    ) {
        assertFalse(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(value.get())
    }
}
