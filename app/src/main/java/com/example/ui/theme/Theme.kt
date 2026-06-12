package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  lightColorScheme(
    primary = TacticalRed,
    onPrimary = Color.White,
    secondary = TacticalRed,
    tertiary = Banner,
    background = BackGround,
    onBackground = TextDark,
    surface = Surface,
    onSurface = TextDark,
    outline = Outline,
    primaryContainer = Surface,
    onPrimaryContainer = TacticalRed,
    surfaceVariant = Banner,
    onSurfaceVariant = TextLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TacticalRed,
    onPrimary = Color.White,
    secondary = TacticalRed,
    tertiary = Banner,
    background = BackGround,
    onBackground = TextDark,
    surface = Surface,
    onSurface = TextDark,
    outline = Outline,
    primaryContainer = Surface,
    onPrimaryContainer = TacticalRed,
    surfaceVariant = Banner,
    onSurfaceVariant = TextLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to preserve cohesive professional identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
