package sp.sample.clicks

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(top = 64.dp, bottom = 64.dp),
            ) {
                val context = LocalContext.current
                val style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Color.White,
                )
                val data = remember { mutableStateOf(listOf<String>()) }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    itemsIndexed(data.value) { index, it ->
                        Row(Modifier.fillMaxWidth().height(56.dp)) {
                            BasicText(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .wrapContentHeight(),
                                text = it,
                                style = style,
                            )
                            BasicText(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .onClick {
                                        context.showToast("on click: $index] $it")
                                    }
                                    .wrapContentHeight(),
                                text = "click",
                                style = style,
                            )
                            BasicText(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .onLongClick {
                                        context.showToast("on long click: $index] $it")
                                    }
                                    .wrapContentHeight(),
                                text = "long click",
                                style = style,
                            )
                            BasicText(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clicks(
                                        onClick = {
                                            context.showToast("on click: $index] $it")
                                        },
                                        onLongClick = {
                                            context.showToast("on long click: $index] $it")
                                        }
                                    )
                                    .wrapContentHeight(),
                                text = "clicks",
                                style = style,
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f),
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)) {
                        BasicText(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clickable {
                                    data.value = data.value + System
                                        .currentTimeMillis()
                                        .toString()
                                }
                                .wrapContentHeight(),
                            text = "+",
                            style = style,
                        )
                        BasicText(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                                .clickable {
                                    if (data.value.isNotEmpty()) {
                                        data.value = data.value.subList(1, data.value.size)
                                    }
                                }
                                .wrapContentHeight(),
                            text = "-",
                            style = style,
                        )
                    }
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
                    BasicText(
                        modifier = Modifier.button {
                            onClick(enabled = true) {
                                context.showToast("on enabled click...")
                            }
                        },
                        text = "enabled",
                        style = style,
                    )
                    BasicText(
                        modifier = Modifier.button {
                            onClick(enabled = false) {
                                error("Impossible!")
                            }
                        },
                        text = "disabled",
                        style = style,
                    )
                }
            }
        }
    }
}
