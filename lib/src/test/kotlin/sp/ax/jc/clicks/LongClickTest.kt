package sp.ax.jc.clicks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

@RunWith(RobolectricTestRunner::class)
internal class LongClickTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun clicksTest() {
        var value = false
        val tag = "clickable"
        rule.setContent {
            Box(
                modifier = Modifier.fillMaxSize()
                    .testTag(tag)
                    .onLongClick {
                        value = !value
                    },
            )
        }
        assertFalse(value)
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertTrue(value)
        rule.onNodeWithTag(tag).performTouchInput {
            longClick()
        }
        assertFalse(value)
    }
}
