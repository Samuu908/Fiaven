package app.rork.un30xjmm47ctw8vs4cms0.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FV {
    val Canvas = Color(0xFFF6F8F6)
    val Surface = Color(0xFFFFFFFF)
    val Green = Color(0xFF2E7D5B)
    val GreenDark = Color(0xFF1F5E43)
    val GreenSoft = Color(0xFFE3F0E9)
    val GreenTint = Color(0xFFEFF6F2)
    val Amber = Color(0xFFE8A33D)
    val AmberText = Color(0xFFB5701A)
    val AmberSoft = Color(0xFFFDF1DE)
    val AmberTint = Color(0xFFFFF8EC)
    val Ink = Color(0xFF1B2A22)
    val Muted = Color(0xFF5F6F66)
    val Hairline = Color(0xFFE2E8E4)
    val Danger = Color(0xFFB3261E)
}

private val LightColors = lightColorScheme(
    primary = FV.Green,
    onPrimary = Color.White,
    primaryContainer = FV.GreenSoft,
    onPrimaryContainer = FV.GreenDark,
    secondary = FV.Amber,
    onSecondary = Color.White,
    secondaryContainer = FV.GreenSoft,
    onSecondaryContainer = FV.GreenDark,
    tertiary = FV.AmberText,
    tertiaryContainer = FV.AmberSoft,
    onTertiaryContainer = FV.AmberText,
    background = FV.Canvas,
    onBackground = FV.Ink,
    surface = FV.Canvas,
    onSurface = FV.Ink,
    surfaceVariant = Color(0xFFEDF1EE),
    onSurfaceVariant = FV.Muted,
    surfaceContainerLowest = FV.Surface,
    surfaceContainerLow = FV.Surface,
    surfaceContainer = FV.Surface,
    surfaceContainerHigh = Color(0xFFF0F3F1),
    surfaceContainerHighest = Color(0xFFE9EEEA),
    outline = Color(0xFFC9D2CC),
    outlineVariant = FV.Hairline,
    error = FV.Danger,
)

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
    MaterialTheme(
        colorScheme = LightColors,
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
