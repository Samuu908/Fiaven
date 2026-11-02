package app.rork.un30xjmm47ctw8vs4cms0

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.rork.un30xjmm47ctw8vs4cms0.ui.navigation.AppNavigation
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.AppTheme
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.ThemePrefs
import androidx.compose.runtime.LaunchedEffect

private fun ComponentActivity.applyBars(dark: Boolean) {
    val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemePrefs.load(this)
        applyBars(dark = false)
        setContent {
            AppTheme {
                // System-bar icons follow the chosen light/dark look.
                val dark = FV.isDark
                LaunchedEffect(dark) { applyBars(dark) }
                AppNavigation()
            }
        }
    }
}
