package com.lyf.cmpdemo.feature.cart.data

import androidx.compose.ui.graphics.Color
import com.lyf.cmpdemo.feature.cart.domain.CartItem

interface CartRepository {
    fun loadCartItems(): List<CartItem>
}

// mock 实现：接真实数据源时换 Ktorfit + Room 实现并改绑 Koin，
// ViewModel 与 UI 零改动
class MockCartRepository : CartRepository {
    override fun loadCartItems(): List<CartItem> = listOf(
        CartItem(1, "挂耳咖啡 · 10 包装", 1250, 2, "☕", Color(0xFFF7E8D3)),
        CartItem(2, "陶瓷马克杯 380ml", 880, 1, "🍵", Color(0xFFE2EFFA)),
        CartItem(3, "无线蓝牙耳机 · 半入耳", 19900, 1, "🎧", Color(0xFFEFE7FB)),
        CartItem(4, "快充充电宝 20000mAh", 12900, 1, "🔋", Color(0xFFE6F4E4)),
        CartItem(5, "棉麻收纳袋 · 三件套", 3990, 3, "🧺", Color(0xFFFCEBDC)),
        CartItem(6, "极简木质台历 2026", 2500, 1, "📅", Color(0xFFE3F1EF)),
    )
}
