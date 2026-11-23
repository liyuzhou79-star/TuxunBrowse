package com.tuxunbrowse.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tuxunbrowse.android.ui.BrowserApp
import com.tuxunbrowse.android.ui.TuxunBrowseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuxunBrowseTheme {
                BrowserApp()
            }
        }
    }
}
