package com.lyf.cmpdemo.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

// 继承多平台版 ViewModel，两端经 viewModel() 获取（lifecycle 2.8+ KMP 化）
// 注意 iOS 侧语义差异：store 靠引用计数清理，VC 被 pop 时 onCleared 可能不触发（CMP-10175），
// 单屏 demo 无资源释放需求，暂不处理
class CartViewModel : ViewModel() {
    // mock 数据；接真实数据源时只改这里
    val items = mutableStateListOf(
        CartItem(1, "挂耳咖啡 · 10 包装", 1250, 2, "☕", Color(0xFFF7E8D3)),
        CartItem(2, "陶瓷马克杯 380ml", 880, 1, "🍵", Color(0xFFE2EFFA)),
        CartItem(3, "无线蓝牙耳机 · 半入耳", 19900, 1, "🎧", Color(0xFFEFE7FB)),
        CartItem(4, "快充充电宝 20000mAh", 12900, 1, "🔋", Color(0xFFE6F4E4)),
        CartItem(5, "棉麻收纳袋 · 三件套", 3990, 3, "🧺", Color(0xFFFCEBDC)),
        CartItem(6, "极简木质台历 2026", 2500, 1, "📅", Color(0xFFE3F1EF)),
    )

    // 派生状态：依赖的 selected 变化时自动重算
    val selectedCount: Int by derivedStateOf { items.count { it.selected } }
    val totalCents: Int by derivedStateOf { items.sumOf { if (it.selected) it.priceCents * it.count else 0 } }
    val allSelected: Boolean by derivedStateOf { items.isNotEmpty() && items.all { it.selected } }

    fun toggleItem(item: CartItem) {
        item.selected = !item.selected
    }

    fun toggleSelectAll() {
        val target = !allSelected
        items.forEach { it.selected = target }
    }
}
