package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AgedGold,
    onPrimary = MidnightInk,
    primaryContainer = Color(0xFF3D311E),
    onPrimaryContainer = SoftGold,
    secondary = TerracottaQuill,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF422621),
    onSecondaryContainer = Color(0xFFFFDAD2),
    tertiary = SageLeaf,
    onTertiary = Color.White,
    background = MidnightInk,
    onBackground = ParchmentLight,
    surface = DeepVelvet,
    onSurface = ParchmentLight,
    surfaceVariant = Color(0xFF2E2747),
    onSurfaceVariant = Color(0xFFD5CBB8)
)

private val LightColorScheme = lightColorScheme(
    primary = AntiqueBronze,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7E2BC),
    onPrimaryContainer = WarmEspresso,
    secondary = TerracottaQuill,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE1D8),
    onSecondaryContainer = Color(0xFF4A1B0D),
    tertiary = SageLeaf,
    onTertiary = Color.White,
    background = ParchmentLight,
    onBackground = WarmEspresso,
    surface = Color(0xFFFFFCF6),
    onSurface = WarmEspresso,
    surfaceVariant = ParchmentCard,
    onSurfaceVariant = MutedSepia
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
