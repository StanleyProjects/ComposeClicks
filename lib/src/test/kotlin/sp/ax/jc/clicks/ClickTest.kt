package sp.ax.jc.clicks

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
internal class ClickTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun onClickDefaultDefaultTest() {
        val value = AtomicBoolean(false)
        val tag = "onClick:default:default"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                    ) {
                        value.set(!value.get())
                    },
            )
        }
        onClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onClickDefaultTest() {
        val value = AtomicBoolean(false)
        val tag = "onClick:default"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick(
                        enabled = true,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                    ) {
                        value.set(!value.get())
                    },
            )
        }
        onClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onClickDefaultDisabledTest() {
        val value = AtomicBoolean(false)
        val tag = "onClick:default:disabled"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick(
                        enabled = false,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                    ) {
                        value.set(!value.get())
                    },
            )
        }
        onClickDisabledAssert(value = value, tag = tag)
    }

    @Test
    fun onClickTest() {
        val value = AtomicBoolean(false)
        val tag = "onClick"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick {
                        value.set(!value.get())
                    },
            )
        }
        onClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onClickEnabledTest() {
        val value = AtomicBoolean(false)
        val tag = "onClick:enabled"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick(enabled = true) {
                        value.set(!value.get())
                    },
            )
        }
        onClickEnabledAssert(value = value, tag = tag)
    }

    @Test
    fun onClickDisabledTest() {
        val value = AtomicBoolean(false)
        val tag = "onClick:disabled"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick(enabled = false) {
                        value.set(!value.get())
                    },
            )
        }
        onClickDisabledAssert(value = value, tag = tag)
    }

    private fun onClickEnabledAssert(
        value: AtomicBoolean,
        tag: String,
    ) {
        assertFalse(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performClick()
        assertTrue(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performClick()
        assertFalse(value.get())
    }

    private fun onClickDisabledAssert(
        value: AtomicBoolean,
        tag: String,
    ) {
        assertFalse(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performClick()
        assertFalse(value.get())
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performClick()
        assertFalse(value.get())
    }
}
