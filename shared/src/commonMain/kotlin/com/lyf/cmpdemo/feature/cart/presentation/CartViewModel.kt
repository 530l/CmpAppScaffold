package com.lyf.cmpdemo.feature.cart.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lyf.cmpdemo.feature.cart.data.CartRepository
import com.lyf.cmpdemo.feature.cart.domain.CartItem

// 单一状态树：UI 只读它渲染；派生量以计算属性表达，state 变化即触发重组
data class CartUiState(val items: List<CartItem> = emptyList()) {
    val selectedCount: Int get() = items.count { it.selected }
    val totalCents: Int get() = items.sumOf { if (it.selected) it.priceCents * it.count else 0 }
    val allSelected: Boolean get() = items.isNotEmpty() && items.all { it.selected }
}

// 用户意图：UI 只发意图，不直接改状态 —— 单向数据流 Intent → reduce → UiState
sealed interface CartIntent {
    data class ToggleItem(val itemId: Int) : CartIntent
    data object ToggleSelectAll : CartIntent
}

class CartViewModel(repository: CartRepository) : ViewModel() {
    var uiState by mutableStateOf(CartUiState(repository.loadCartItems()))
        private set

    fun onIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.ToggleItem -> updateItems { item ->
                if (item.id == intent.itemId) item.copy(selected = !item.selected) else item
            }
            is CartIntent.ToggleSelectAll -> {
                val target = !uiState.allSelected
                updateItems { it.copy(selected = target) }
            }
        }
    }

    // copy-on-write：map 出新列表并整体替换 UiState（不可变数据 + 状态提升）
    private fun updateItems(transform: (CartItem) -> CartItem) {
        uiState = uiState.copy(items = uiState.items.map(transform))
    }
}
