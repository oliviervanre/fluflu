package fr.claynum.fluflu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF16312E)
val Muted = Color(0xFF657774)
val Paper = Color(0xFFFBFCF8)
val Surface = Color(0xFFFFFFFF)
val Line = Color(0xFFDCE5E1)
val Green = Color(0xFF1D5E55)
val GreenDark = Color(0xFF173F3A)
val GreenSoft = Color(0xFFE3F1ED)
val Orange = Color(0xFFA84D2E)
val OrangeSoft = Color(0xFFFCE9DF)

private val FluFluColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = Ink,
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = OrangeSoft,
    onSecondaryContainer = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    outline = Line
)

@Composable
fun FluFluTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FluFluColors,
        typography = MaterialTheme.typography.copy(
            headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp),
            headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
            titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp),
            titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
            bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
            bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp)
        ),
        content = content
    )
}
