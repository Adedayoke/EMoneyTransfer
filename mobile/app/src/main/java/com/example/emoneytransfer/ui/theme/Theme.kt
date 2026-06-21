package com.example.emoneytransfer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = MintOnDark,
    primaryContainer = Color(0xFF004D3A),
    onPrimaryContainer = Color(0xFF9DFFD5),
    secondary = MintDim,
    onSecondary = Color(0xFF003828),
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2A2A2A),
    error = ErrorRed,
    onError = Color.White,
    scrim = Color(0xFF000000),
)

@Composable
fun EMoneyTransferTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
