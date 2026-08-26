package com.lyf.cmpdemo.feature.cart.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    val id: Long,
    val name: String,
    val priceMinor: Long,
    val currencyCode: String,
    val count: Int,
    val emoji: String,
    val imageUrl: String? = null,
    val selected: Boolean = false,
)
