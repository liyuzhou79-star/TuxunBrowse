package com.tuxunbrowse.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tuxunbrowse.android.data.Store
import com.tuxunbrowse.android.data.ThemeMode
import com.tuxunbrowse.android.ui.BrowserApp
import com.tuxunbrowse.android.ui.TuxunBrowseTheme

class MainActivity : ComponentActivity() {

    /** A web link handed to us by another app (we are a browser). */
    private var externalUrl by mutableStateOf<String?>(null)

    /**
     * The one theme-mode state the whole activity reads: MainActivity
     * picks the colour scheme from it and BrowserApp's settings page
     * writes it (through the callback below), so changing the setting
     * re-themes every screen at once — no screen can lag behind.
     */
    private var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        themeMode = Store(this).themeMode
        externalUrl = intent?.dataString?.takeIf { it.startsWith("http") }
        setContent {
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            TuxunBrowseTheme(darkTheme = darkTheme) {
                BrowserApp(
                    initialUrl = externalUrl,
                    onInitialUrlConsumed = { externalUrl = null },
                    onThemeModeChanged = { themeMode = it }
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
