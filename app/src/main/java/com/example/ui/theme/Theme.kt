package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class SnixlyColors(
  val isDark: Boolean,
  val background: Color,
  val surface: Color,
  val surfaceVariant: Color,
  val primaryText: Color,
  val secondaryText: Color,
  val border: Color,
  val accentGold: Color,
  val cardBackground: Color
)

val LocalSnixlyColors = staticCompositionLocalOf {
  SnixlyColors(
    isDark = false,
    background = SnixlyLightBackground,
    surface = SnixlyLightSurface,
    surfaceVariant = SnixlyLightSurfaceVariant,
    primaryText = SnixlyLightPrimaryText,
    secondaryText = SnixlyLightSecondaryText,
    border = SnixlyLightBorder,
    accentGold = SnixlyGoldPrimary,
    cardBackground = SnixlyLightSurface
  )
}

val MaterialTheme.snixly: SnixlyColors
  @Composable
  get() = LocalSnixlyColors.current

private val DarkColorScheme =
  darkColorScheme(
    primary = SnixlyGoldBright,
    onPrimary = Color(0xFF100E07),
    primaryContainer = SnlyDarkSurfaceVariant(SnixlyDarkSurfaceElevated),
    onPrimaryContainer = SnixlyGoldChampagne,
    secondary = SnixlyGoldSoft,
    onSecondary = Color(0xFF100E07),
    background = SnixlyDarkBackground,
    onBackground = SnixlyDarkPrimaryText, // Crisp Pure White
    surface = SnixlyDarkSurface,
    onSurface = SnixlyDarkPrimaryText, // Crisp Pure White
    surfaceVariant = SnixlyDarkSurfaceVariant,
    onSurfaceVariant = SnixlyDarkSecondaryText, // Bright Silver Gray
    outline = SnixlyDarkBorder,
    outlineVariant = SnixlyDarkBorder.copy(alpha = 0.6f)
  )

private fun SnlyDarkSurfaceVariant(color: Color): Color = color

private val LightColorScheme =
  lightColorScheme(
    primary = SnixlyGoldPrimary,
    onPrimary = Color.White,
    primaryContainer = SnixlyGoldChampagne,
    onPrimaryContainer = SnixlyGoldDeep,
    secondary = SnixlyGoldDeep,
    onSecondary = Color.White,
    background = SnixlyLightBackground,
    onBackground = SnixlyLightPrimaryText, // Crisp Deep Black
    surface = SnixlyLightSurface,
    onSurface = SnixlyLightPrimaryText, // Crisp Deep Black
    surfaceVariant = SnixlyLightSurfaceVariant,
    onSurfaceVariant = SnixlyLightSecondaryText, // High Contrast Slate
    outline = SnixlyLightBorder,
    outlineVariant = SnixlyLightBorder.copy(alpha = 0.6f)
  )

@Composable
fun SnixlyTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val snixlyPalette = if (darkTheme) {
    SnixlyColors(
      isDark = true,
      background = SnixlyDarkBackground,
      surface = SnixlyDarkSurface,
      surfaceVariant = SnixlyDarkSurfaceVariant,
      primaryText = SnixlyDarkPrimaryText, // Pure White on Dark
      secondaryText = SnixlyDarkSecondaryText, // Silver on Dark
      border = SnixlyDarkBorder,
      accentGold = SnixlyGoldBright,
      cardBackground = SnixlyDarkSurfaceElevated
    )
  } else {
    SnixlyColors(
      isDark = false,
      background = SnixlyLightBackground,
      surface = SnixlyLightSurface,
      surfaceVariant = SnixlyLightSurfaceVariant,
      primaryText = SnixlyLightPrimaryText, // Deep Black on Light
      secondaryText = SnixlyLightSecondaryText, // Slate on Light
      border = SnixlyLightBorder,
      accentGold = SnixlyGoldPrimary,
      cardBackground = SnixlyLightSurface
    )
  }

  CompositionLocalProvider(LocalSnixlyColors provides snixlyPalette) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  SnixlyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
