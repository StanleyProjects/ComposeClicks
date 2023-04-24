package sp.sample.clicks

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sp.ax.jc.clicks.clicks
import sp.ax.jc.clicks.onClick
import sp.ax.jc.clicks.onLongClick

private fun Modifier.button(block: Modifier.() -> Modifier): Modifier {
    return fillMaxWidth()
        .height(56.dp)
        .block()
        .wrapContentHeight()
}

internal class MainActivity : AppCompatActivity() {
    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .align(Alignment.Center),
                ) {
                    val context = LocalContext.current
                    val style = TextStyle(
                        textAlign = TextAlign.Center,
                        color = Color.White,
                    )
                    BasicText(
                        modifier = Modifier.button {
                            onClick {
                                context.showToast("on click...")
                            }
                        },
                        text = "click",
                        style = style,
                    )
                    BasicText(
                        modifier = Modifier.button {
                            onLongClick {
                                context.showToast("on long click...")
                            }
                        },
                        text = "long click",
                        style = style,
                    )
                    BasicText(
                        modifier = Modifier.button {
                            clicks(
                                onClick = {
                                    context.showToast("on click...")
                                },
                                onLongClick = {
                                    context.showToast("on long click...")
                                },
                            )
                        },
                        text = "clicks",
                        style = style,
                    )
                }
            }
        }
    }
}
