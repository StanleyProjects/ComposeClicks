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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
internal class ClicksTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun clicksDefaultTest() {
        val click = AtomicBoolean(false)
        val longClick = AtomicBoolean(false)
        val tag = "clicks:default"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .clicks(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            click.set(!click.get())
                        },
                        onLongClick = {
                            longClick.set(!longClick.get())
                        },
                    ),
            )
        }
        clicksEnabledAssert(click = click, longClick = longClick, tag = tag)
    }

    @Test
    fun clicksDefaultEnabledTest() {
        val click = AtomicBoolean(false)
        val longClick = AtomicBoolean(false)
        val tag = "clicks:default:enabled"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .clicks(
                        enabled = true,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            click.set(!click.get())
                        },
                        onLongClick = {
                            longClick.set(!longClick.get())
                        },
                    ),
            )
        }
        clicksEnabledAssert(click = click, longClick = longClick, tag = tag)
    }

    @Test
    fun clicksDefaultDisabledTest() {
        val click = AtomicBoolean(false)
        val longClick = AtomicBoolean(false)
        val tag = "clicks:default:disabled"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .clicks(
                        enabled = false,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            click.set(!click.get())
                        },
                        onLongClick = {
                            longClick.set(!longClick.get())
                        },
                    ),
            )
        }
        clicksDisabledAssert(click = click, longClick = longClick, tag = tag)
    }

    @Suppress("MemberNameEqualsClassName")
    @Test
    fun clicksTest() {
        val click = AtomicBoolean(false)
        val longClick = AtomicBoolean(false)
        val tag = "clicks"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .clicks(
                        onClick = {
                            click.set(!click.get())
                        },
                        onLongClick = {
                            longClick.set(!longClick.get())
                        },
                    ),
            )
        }
        clicksEnabledAssert(click = click, longClick = longClick, tag = tag)
    }

    @Test
    fun clicksEnabledTest() {
        val click = AtomicBoolean(false)
        val longClick = AtomicBoolean(false)
        val tag = "clicks:enabled"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .clicks(
                        enabled = true,
                        onClick = {
                            click.set(!click.get())
                        },
                        onLongClick = {
                            longClick.set(!longClick.get())
                        },
                    ),
            )
        }
        clicksEnabledAssert(click = click, longClick = longClick, tag = tag)
    }

    @Test
    fun clicksDisabledTest() {
        val click = AtomicBoolean(false)
        val longClick = AtomicBoolean(false)
        val tag = "clicks:disabled"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .clicks(
                        enabled = false,
                        onClick = {
                            click.set(!click.get())
                        },
                        onLongClick = {
                            longClick.set(!longClick.get())
                        },
                    ),
            )
        }
        clicksDisabledAssert(click = click, longClick = longClick, tag = tag)
    }

    private fun clicksEnabledAssert(
        click: AtomicBoolean,
        longClick: AtomicBoolean,
        tag: String,
    ) {
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        assertTrue(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(click.get())
        assertTrue(longClick.get())
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertTrue(click.get())
        assertTrue(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(click.get())
        assertFalse(longClick.get())
    }

    private fun clicksDisabledAssert(
        click: AtomicBoolean,
        longClick: AtomicBoolean,
        tag: String,
    ) {
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(click.get())
        assertFalse(longClick.get())
        rule.onNodeWithTag(tag).performClick()
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(click.get())
        assertFalse(longClick.get())
    }
}
