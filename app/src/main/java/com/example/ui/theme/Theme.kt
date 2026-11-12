package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = EduCyan,
  onPrimary = EduNavyDark,
  primaryContainer = EduNavyMedium,
  onPrimaryContainer = EduCyanLight,
  secondary = EduGold,
  onSecondary = Color(0xFF2E1700),
  secondaryContainer = Color(0xFF452600),
  onSecondaryContainer = EduGoldLight,
  tertiary = EduBlueAccent,
  onTertiary = Color.White,
  background = EduBackgroundDark,
  onBackground = EduTextPrimaryDark,
  surface = EduSurfaceDark,
  onSurface = EduTextPrimaryDark,
  surfaceVariant = EduSurfaceVariantDark,
  onSurfaceVariant = EduTextSecondaryDark,
  outline = EduBorderDark,
  error = EduError,
  errorContainer = Color(0xFF450A0A),
  onError = Color.White,
  onErrorContainer = EduErrorContainer
)

private val LightColorScheme = lightColorScheme(
  primary = EduNavy,
  onPrimary = Color.White,
  primaryContainer = EduNavyLight,
  onPrimaryContainer = Color.White,
  secondary = EduGoldDark,
  onSecondary = Color.White,
  secondaryContainer = EduGoldLight,
  onSecondaryContainer = Color(0xFF78350F),
  tertiary = EduCyanDark,
  onTertiary = Color.White,
  tertiaryContainer = EduCyanLight,
  onTertiaryContainer = EduNavyDark,
  background = EduBackgroundLight,
  onBackground = EduTextPrimaryLight,
  surface = EduSurfaceLight,
  onSurface = EduTextPrimaryLight,
  surfaceVariant = EduSurfaceVariantLight,
  onSurfaceVariant = EduTextSecondaryLight,
  outline = EduBorderLight,
  error = EduError,
  errorContainer = EduErrorContainer,
  onError = Color.White,
  onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun EdukazaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent branding colors
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
