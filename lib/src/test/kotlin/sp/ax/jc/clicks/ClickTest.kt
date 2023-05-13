package sp.ax.jc.clicks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

@RunWith(RobolectricTestRunner::class)
internal class ClickTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun onClickTest() {
        var value = false
        val tag = "clickable"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick {
                        value = !value
                    },
            )
        }
        assertFalse(value)
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performClick()
        assertTrue(value)
        @Suppress("IgnoredReturnValue")
        rule.onNodeWithTag(tag).performClick()
        assertFalse(value)
    }
}
