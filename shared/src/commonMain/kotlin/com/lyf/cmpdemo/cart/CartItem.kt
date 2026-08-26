package com.lyf.cmpdemo.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// body 里声明的属性不参与 data class 生成的 equals/copy，见 selected 处注释
data class CartItem(
    val id: Int,
    val name: String,
    /** 单价，单位：分（展示时再换算为元，避免浮点误差） */
    val priceCents: Int,
    val count: Int,
    val emoji: String,
    val bgColor: Color,
) {
    // Compose 可观察状态：值变化时引用它的 UI 自动重组刷新。
    // 声明在 body 里，因此不参与 data class 生成的 equals/copy
    var selected by mutableStateOf(false)
}
