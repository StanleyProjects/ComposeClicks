package sp.ax.clicks.sample

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sp.ax.clicks.clicks

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
                    BasicText(
                        modifier = Modifier.fillMaxWidth()
                            .height(56.dp)
                            .clicks(
                                onClick = {
                                    context.showToast("on click...")
                                },
                                onLongClick = {
                                    context.showToast("on long click...")
                                },
                            )
                            .wrapContentHeight(),
                        text = "clicks",
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = Color.White,
                        ),
                    )
                }
            }
        }
    }
}
