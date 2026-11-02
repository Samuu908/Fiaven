package app.rork.un30xjmm47ctw8vs4cms0.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** User's appearance choice; persisted by [ThemePrefs]. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private class Palette(
    val canvas: Color, val surface: Color, val green: Color, val greenDark: Color, val greenSoft: Color, val greenTint: Color,
    val amber: Color, val amberText: Color, val amberSoft: Color, val amberTint: Color, val ink: Color, val muted: Color,
    val hairline: Color, val danger: Color, val dangerSoft: Color, val dangerTint: Color, val neutral: Color, val outline: Color,
    val onGreen: Color,
)

private val LightPalette = Palette(
    canvas = Color(0xFFF6F8F6), surface = Color(0xFFFFFFFF),
    green = Color(0xFF2E7D5B), greenDark = Color(0xFF1F5E43), greenSoft = Color(0xFFE3F0E9), greenTint = Color(0xFFEFF6F2),
    amber = Color(0xFFE8A33D), amberText = Color(0xFFB5701A), amberSoft = Color(0xFFFDF1DE), amberTint = Color(0xFFFFF8EC),
    ink = Color(0xFF1B2A22), muted = Color(0xFF5F6F66), hairline = Color(0xFFE2E8E4),
    danger = Color(0xFFB3261E), dangerSoft = Color(0xFFF9DEDC), dangerTint = Color(0xFFFDF1F0),
    neutral = Color(0xFFF0F3F1), outline = Color(0xFFD5DDD8), onGreen = Color(0xFFFFFFFF),
)

/** Night version: deep green-black canvas, same green/amber/red roles, lifted for contrast. */
private val DarkPalette = Palette(
    canvas = Color(0xFF0E1411), surface = Color(0xFF171F1B),
    green = Color(0xFF5DBE8D), greenDark = Color(0xFF8FD8B2), greenSoft = Color(0xFF1E3A2C), greenTint = Color(0xFF14261D),
    amber = Color(0xFFE8A33D), amberText = Color(0xFFF2B860), amberSoft = Color(0xFF3B2C15), amberTint = Color(0xFF231C10),
    ink = Color(0xFFE4ECE7), muted = Color(0xFF98A9A0), hairline = Color(0xFF26312B),
    danger = Color(0xFFF2B8B5), dangerSoft = Color(0xFF4A201D), dangerTint = Color(0xFF2A1614),
    neutral = Color(0xFF212B26), outline = Color(0xFF3A4741), onGreen = Color(0xFF0B1A12),
)

object FV {
    private var palette by mutableStateOf(LightPalette)
    var isDark: Boolean
        get() = palette === DarkPalette
        internal set(value) { val p = if (value) DarkPalette else LightPalette; if (palette !== p) palette = p }

    val Canvas get() = palette.canvas
    val Surface get() = palette.surface
    val Green get() = palette.green
    val GreenDark get() = palette.greenDark
    val GreenSoft get() = palette.greenSoft
    val GreenTint get() = palette.greenTint
    val Amber get() = palette.amber
    val AmberText get() = palette.amberText
    val AmberSoft get() = palette.amberSoft
    val AmberTint get() = palette.amberTint
    val Ink get() = palette.ink
    val Muted get() = palette.muted
    val Hairline get() = palette.hairline
    val Danger get() = palette.danger
    /** Overdue debt (> 15 days) backgrounds. */
    val DangerSoft get() = palette.dangerSoft
    val DangerTint get() = palette.dangerTint
    /** Neutral icon-badge background. */
    val Neutral get() = palette.neutral
    /** Unfocused input border. */
    val Outline get() = palette.outline
    /** Text/icons on solid green. */
    val OnGreen get() = palette.onGreen
}

object ThemePrefs {
    private const val KEY = "theme_mode"
    var mode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun load(context: Context) {
        val raw = context.getSharedPreferences("fiaven", Context.MODE_PRIVATE).getString(KEY, null)
        mode = ThemeMode.entries.find { it.name == raw } ?: ThemeMode.SYSTEM
    }

    fun set(context: Context, value: ThemeMode) {
        mode = value
        context.getSharedPreferences("fiaven", Context.MODE_PRIVATE).edit().putString(KEY, value.name).apply()
    }
}

private fun colorsFor(p: Palette, dark: Boolean) = (if (dark) darkColorScheme() else lightColorScheme()).copy(
    primary = p.green,
    onPrimary = p.onGreen,
    primaryContainer = p.greenSoft,
    onPrimaryContainer = p.greenDark,
    secondary = p.amber,
    onSecondary = p.onGreen,
    secondaryContainer = p.greenSoft,
    onSecondaryContainer = p.greenDark,
    tertiary = p.amberText,
    tertiaryContainer = p.amberSoft,
    onTertiaryContainer = p.amberText,
    background = p.canvas,
    onBackground = p.ink,
    surface = p.canvas,
    onSurface = p.ink,
    surfaceVariant = p.neutral,
    onSurfaceVariant = p.muted,
    surfaceContainerLowest = p.surface,
    surfaceContainerLow = p.surface,
    surfaceContainer = p.surface,
    surfaceContainerHigh = if (dark) Color(0xFF1D2621) else Color(0xFFF0F3F1),
    surfaceContainerHighest = if (dark) Color(0xFF232D28) else Color(0xFFE9EEEA),
    inverseSurface = p.ink,
    inverseOnSurface = p.canvas,
    inversePrimary = p.greenDark,
    outline = p.outline,
    outlineVariant = p.hairline,
    error = p.danger,
    onError = p.surface,
)

private val LightColors = colorsFor(LightPalette, false)
private val DarkColors = colorsFor(DarkPalette, true)

private val base = Typography()
private val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = (-1).sp),
    displayMedium = base.displayMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Medium),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Medium),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)

val MoneyHuge = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp, lineHeight = 50.sp)
val MoneyLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp, lineHeight = 40.sp)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = when (ThemePrefs.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    FV.isDark = dark
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
