package com.lyf.cmpdemo.core.util

// 价格按分存整数避免浮点误差，展示时换算为元
fun formatPrice(cents: Int): String {
    val yuan = cents / 100
    val fen = cents % 100
    return "¥$yuan.${if (fen < 10) "0$fen" else "$fen"}"
}
