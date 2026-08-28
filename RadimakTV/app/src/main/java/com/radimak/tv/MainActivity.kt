package com.radimak.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.radimak.tv.ui.AppViewModel
import com.radimak.tv.ui.RadimakTvApp
import com.radimak.tv.ui.theme.RadimakTvTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadimakTvTheme {
                RadimakTvApp(viewModel)
            }
        }
    }
}
