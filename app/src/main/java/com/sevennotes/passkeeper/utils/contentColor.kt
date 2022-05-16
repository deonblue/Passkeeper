package com.sevennotes.passkeeper.utils

import androidx.compose.ui.graphics.Color

fun contentColor(color: Color): Color {
    val r = color.red * 255
    val g = color.green * 255
    val b = color.blue * 255
    val gray = r * 0.299 + g * 0.587 + b * 0.114
    return if (gray > 186) Color.Black else Color.White
}