package com.tuxunbrowse.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tuxunbrowse.android.ui.BrowserApp
import com.tuxunbrowse.android.ui.TuxunBrowseTheme

class MainActivity : ComponentActivity() {

    /** A web link handed to us by another app (we are a browser). */
    private var externalUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        externalUrl = intent?.dataString?.takeIf { it.startsWith("http") }
        setContent {
            TuxunBrowseTheme {
                BrowserApp(
                    initialUrl = externalUrl,
                    onInitialUrlConsumed = { externalUrl = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        externalUrl = intent.dataString?.takeIf { it.startsWith("http") }
    }
}
