package sp.ax.jc.clicks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

internal class ClickTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun clickTest() {
        var value = false
        val tag = "clickable"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onClick {
                        value = !value
                    }
            )
        }
        assertFalse(value)
        rule.onNodeWithTag(tag).performClick()
        assertTrue(value)
        rule.onNodeWithTag(tag).performClick()
        assertFalse(value)
    }
}
