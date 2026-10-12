package pe.edu.upeu.pharmamobil.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Identidad visual de PharmaMobil: fondo #FEFEFE, acento #2FB69F y superficies
 * de vidrio translucido (ver Vidrio.kt) sobre un degradado suave. El ambar
 * queda reservado para avisos.
 */
val TealMarca = Color(0xFF2FB69F)
val TealOscuro = Color(0xFF1E8C79)

private val LightColors = lightColorScheme(
    primary = TealMarca,
    onPrimary = Color(0xFF032B25),
    primaryContainer = Color(0xFFD9F4EE),
    onPrimaryContainer = Color(0xFF0B4A40),
    secondary = Color(0xFF4F6561),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F1EF),
    onSecondaryContainer = Color(0xFF16322D),
    tertiary = Color(0xFFB45309),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF451A03),
    error = Color(0xFFD64545),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFDE8E8),
    onErrorContainer = Color(0xFF7A1F1F),
    background = Color(0xFFFEFEFE),
    onBackground = Color(0xFF0E1F1C),
    surface = Color(0xFFFEFEFE),
    onSurface = Color(0xFF0E1F1C),
    surfaceVariant = Color(0xFFE9F1EF),
    onSurfaceVariant = Color(0xFF55706B),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xCCFFFFFF),
    surfaceContainer = Color(0xCCFFFFFF),
    surfaceContainerHigh = Color(0xFFF1F6F5),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = Color(0xFF8AA09B),
    outlineVariant = Color(0x332FB69F)
)

private val DarkColors = darkColorScheme(
    primary = TealMarca,
    onPrimary = Color(0xFF032B25),
    primaryContainer = Color(0xFF14463E),
    onPrimaryContainer = Color(0xFFCFF5EC),
    secondary = Color(0xFF9DB5B0),
    onSecondary = Color(0xFF0B1A18),
    secondaryContainer = Color(0xFF1C302D),
    onSecondaryContainer = Color(0xFFDDEEEA),
    tertiary = Color(0xFFFCD34D),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF08110F),
    onBackground = Color(0xFFE6F0EE),
    surface = Color(0xFF0C1715),
    onSurface = Color(0xFFE6F0EE),
    surfaceVariant = Color(0xFF1B2B28),
    onSurfaceVariant = Color(0xFF93ADA8),
    surfaceContainerLowest = Color(0xFF08110F),
    surfaceContainerLow = Color(0xFF0C1715),
    surfaceContainer = Color(0xFF101D1A),
    surfaceContainerHigh = Color(0xFF162522),
    surfaceContainerHighest = Color(0xFF172522),
    outline = Color(0xFF5E7873),
    outlineVariant = Color(0x3D2FB69F)
)

private val Tipografia = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        labelMedium = labelMedium.copy(fontWeight = FontWeight.Medium),
        bodyMedium = bodyMedium.copy(lineHeight = 21.sp)
    )
}

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    BarrasDelSistema(darkTheme)

    MaterialTheme(
        colorScheme = colors,
        typography = Tipografia,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}
