package com.wings.picexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.wings.picexchange.ui.PicExchangeRoot
import com.wings.picexchange.ui.theme.PicExchangeTheme

/** The single Activity that hosts all Compose UI (single-Activity architecture). */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PicExchangeTheme {
                PicExchangeRoot()
            }
        }
    }
}
