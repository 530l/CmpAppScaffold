package com.lyf.cmpappscaffold.feature.cart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val priceMinor: Long,
    val currencyCode: String,
    val count: Int,
    val emoji: String,
    val imageUrl: String?,
    val selected: Boolean,
)
