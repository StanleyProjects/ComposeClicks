package sp.ax.clicks.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

internal class MainActivity : AppCompatActivity() {
    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                // todo
            }
        }
    }
}
