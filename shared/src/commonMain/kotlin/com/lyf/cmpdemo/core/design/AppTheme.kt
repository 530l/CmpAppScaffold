package com.lyf.cmpdemo.core.design

import androidx.compose.ui.graphics.Color

// 全局视觉常量：接入 MaterialTheme colorScheme / 深色模式时在此收敛，
// 各 feature 不得自建颜色字面量
object AppColors {
    val AccentRed = Color(0xFFE93B3D)
    val PageBg = Color(0xFFF6F6F8)
    val CardBg = Color.White
    val TitleText = Color(0xFF1B1B1B)
    val SubText = Color(0xFF9A9A9A)
}
