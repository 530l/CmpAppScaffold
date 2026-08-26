package com.lyf.cmp.feature.cart.domain

import com.lyf.cmp.core.model.Money

/** 领域模型不依赖 Compose 或其他 UI 类型。 */
data class CartItem(
    val id: Long,
    val name: String,
    val unitPrice: Money,
    val count: Int,
    val emoji: String,
    val imageUrl: String? = null,
    val selected: Boolean = false,
) {
    init {
        require(id > 0) { "商品 ID 必须大于 0" }
        require(name.isNotBlank()) { "商品名称不能为空" }
        require(count > 0) { "商品数量必须大于 0" }
    }
}
