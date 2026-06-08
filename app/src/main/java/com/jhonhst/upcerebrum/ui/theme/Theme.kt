package com.jhonhst.upcerebrum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable


// Paleta unificada e idéntica para mantener el estilo oscuro premium en ambos modos
private val UpcerebrumSharedColorScheme = darkColorScheme(
    primary = AreaCyan,
    onPrimary = CerebrumBg,
    secondary = AreaGreen,
    onSecondary = CerebrumBg,
    tertiary = AreaYellow,
    onTertiary = CerebrumBg,

    background = CerebrumBg,
    onBackground = TextPrimary,
    surface = CerebrumSurface,
    onSurface = TextPrimary,
    surfaceVariant = CerebrumSurfaceLight,
    onSurfaceVariant = TextPrimary,

    error = GameError,
    onError = TextPrimary,
    outline = CerebrumBorder
)

@Composable
fun UpcerebrumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para garantizar que se usen tus colores exactos
    content: @Composable () -> Unit
) {
    // Usamos la misma paleta compartida tanto para Light como para Dark
    val colorScheme = UpcerebrumSharedColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}