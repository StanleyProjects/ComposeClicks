package sp.sample.clicks

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sp.ax.jc.clicks.clicks
import sp.ax.jc.clicks.onClick
import sp.ax.jc.clicks.onLongClick

@Composable
private fun Buttons(
    color: Color,
    textStyle: TextStyle,
    vararg pairs: Pair<String, Modifier.() -> Modifier>,
) {
    check(pairs.isNotEmpty())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color),
    ) {
        pairs.forEach { (text, block) ->
            BasicText(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .block()
                    .wrapContentHeight(),
                text = text,
                style = textStyle,
            )
        }
    }
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
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
                                    .background(Color.White)
                                    .weight(1f)
                                    .onClick(enabled = true) {
                                        context.showToast("on click: $index] $it")
                                    }
                                    .wrapContentHeight(),
                                text = "click",
                                style = style.copy(Color.Black),
                            )
                            BasicText(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(Color.Yellow)
                                    .weight(1f)
                                    .onLongClick {
                                        context.showToast("on long click: $index] $it")
                                    }
                                    .wrapContentHeight(),
                                text = "long click",
                                style = style.copy(Color.Black),
                            )
                            BasicText(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(Color.Cyan)
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
                                style = style.copy(Color.Black),
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
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
                    Buttons(
                        color = Color.White,
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                        ),
                        "click" to {
                            onClick(enabled = true) {
                                context.showToast("on enabled click...")
                            }
                        },
                        "click disabled" to {
                            onClick(enabled = false) {
                                error("Disabled!")
                            }
                        }
                    )
                    Buttons(
                        color = Color.Yellow,
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                        ),
                        "long click" to {
                            onLongClick(enabled = true) {
                                context.showToast("on enabled long click...")
                            }
                        },
                        "long click disabled" to {
                            onLongClick(enabled = false) {
                                error("Disabled!")
                            }
                        }
                    )
                    Buttons(
                        color = Color.Cyan,
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                        ),
                        "clicks" to {
                            clicks(
                                enabled = true,
                                onClick = {
                                    context.showToast("on enabled clicks click...")
                                },
                                onLongClick = {
                                    context.showToast("on enabled clicks long click...")
                                },
                            )
                        },
                        "clicks disabled" to {
                            clicks(
                                enabled = false,
                                onClick = {
                                    error("Disabled!")
                                },
                                onLongClick = {
                                    error("Disabled!")
                                },
                            )
                        }
                    )
                }
            }
        }
    }
}
