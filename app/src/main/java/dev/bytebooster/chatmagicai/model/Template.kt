package dev.bytebooster.chatmagicai.model

import androidx.annotation.DrawableRes

data class Template(
    val title: String,
    val desc: String,
    @DrawableRes val image: Int,
)