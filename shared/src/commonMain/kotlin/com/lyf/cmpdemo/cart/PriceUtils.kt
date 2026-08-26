package com.lyf.cmpdemo.cart

fun formatPrice(cents: Int): String {
    val yuan = cents / 100
    val fen = cents % 100
    return "¥$yuan.${if (fen < 10) "0$fen" else "$fen"}"
}
