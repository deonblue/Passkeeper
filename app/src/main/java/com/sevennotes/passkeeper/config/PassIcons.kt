package com.sevennotes.passkeeper.config

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.sevennotes.passkeeper.R

data class CateImg(
    val id: Int,
    val img: Painter
)

@Composable
fun passIcons(): List<CateImg> {
    return listOf(
        CateImg(id = 0, img = painterResource(id = R.drawable.ic___user)),
        CateImg(id = 1, img = painterResource(id = R.drawable.ic___bag)),
        CateImg(id = 2, img = painterResource(id = R.drawable.ic___card)),
        CateImg(id = 3, img = painterResource(id = R.drawable.ic___coupon)),
        CateImg(id = 4, img = painterResource(id = R.drawable.ic___dimond)),
        CateImg(id = 5, img = painterResource(id = R.drawable.ic___edit)),
        CateImg(id = 6, img = painterResource(id = R.drawable.ic___email)),
        CateImg(id = 7, img = painterResource(id = R.drawable.ic___game)),
        CateImg(id = 8, img = painterResource(id = R.drawable.ic___life)),
        CateImg(id = 9, img = painterResource(id = R.drawable.ic___archives)),
        CateImg(id = 10, img = painterResource(id = R.drawable.ic___comment)),
        CateImg(id = 11, img = painterResource(id = R.drawable.ic___data)),
    )
}