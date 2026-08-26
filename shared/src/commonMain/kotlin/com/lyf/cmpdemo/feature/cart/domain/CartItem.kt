package com.lyf.cmpdemo.feature.cart.domain

import androidx.compose.ui.graphics.Color

// 领域模型：纯不可变数据（MVI 下选中态由 ViewModel 以 copy-on-write 更新，
// 模型自身不持有可观察状态）
// bgColor 属展示属性，当前直接持有 Compose Color；如需脱离 UI 层复用可改存 Long
data class CartItem(
    val id: Int,
    val name: String,
    /** 单价，单位：分（展示时再换算为元，避免浮点误差） */
    val priceCents: Int,
    val count: Int,
    val emoji: String,
    val bgColor: Color,
    val selected: Boolean = false,
)
