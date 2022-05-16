package com.sevennotes.passkeeper.ui.theme

import androidx.compose.material.contentColorFor
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import com.sevennotes.passkeeper.utils.contentColor

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

fun lightColorPalette(
    primary: Color = Purple500,
) = lightColors(
    primary = primary,
    primaryVariant = primary,
    secondary = Teal200,
    surface = Color.White,
    background = Color(0xFFCCCCCC),
    onPrimary = contentColor(primary)
    /* Other default colors to override
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)