package io.oryxen.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.oryxen.mobile.ui.OryxenNavHost
import io.oryxen.mobile.ui.theme.OryxenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OryxenTheme {
                OryxenNavHost()
            }
        }
    }
}
