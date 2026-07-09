package io.oryxen.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.ui.OryxenNavHost
import io.oryxen.mobile.ui.theme.OryxenTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.oryxen.mobile.data.AppPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiProvider.init(this)
        setContent {
            val isDarkTheme by AppPreferences.isDarkMode.collectAsState()
            OryxenTheme(darkTheme = isDarkTheme) {
                OryxenNavHost()
            }
        }
    }
}
