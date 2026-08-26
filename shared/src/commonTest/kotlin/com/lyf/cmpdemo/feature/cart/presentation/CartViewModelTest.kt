package com.lyf.cmpdemo.feature.cart.presentation

import com.lyf.cmpdemo.core.util.formatPrice
import com.lyf.cmpdemo.feature.cart.data.MockCartRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ViewModel 核心行为回归（MVI：通过 onIntent 驱动、断言 UiState）。
// 断言金额与 MockCartRepository 数据耦合：1+2 号合计 3380 分、全选合计 50650 分
class CartViewModelTest {

    private fun viewModel() = CartViewModel(MockCartRepository())

    @Test
    fun formatPriceConvertsCentsToYuanString() {
        assertEquals("¥0.00", formatPrice(0))
        assertEquals("¥0.05", formatPrice(5))
        assertEquals("¥33.80", formatPrice(3380))
        assertEquals("¥125.00", formatPrice(12500))
    }

    @Test
    fun totalCentsSumsOnlySelectedItems() {
        val viewModel = viewModel()
        viewModel.onIntent(CartIntent.ToggleItem(1)) // 挂耳咖啡 1250 × 2 = 2500
        viewModel.onIntent(CartIntent.ToggleItem(2)) // 马克杯 880 × 1 = 880
        assertEquals(2, viewModel.uiState.selectedCount)
        assertEquals(3380, viewModel.uiState.totalCents)
        assertEquals("¥33.80", formatPrice(viewModel.uiState.totalCents))
        assertFalse(viewModel.uiState.allSelected)
    }

    @Test
    fun toggleSelectAllFromPartialSelectionSelectsAll() {
        val viewModel = viewModel()
        viewModel.onIntent(CartIntent.ToggleItem(1)) // 1/6 选中：半选态
        assertFalse(viewModel.uiState.allSelected)

        viewModel.onIntent(CartIntent.ToggleSelectAll) // 半选态应走向全选，而不是清空
        assertTrue(viewModel.uiState.allSelected)
        assertEquals(6, viewModel.uiState.selectedCount)
    }

    @Test
    fun toggleSelectAllSwitchesBetweenAllAndNone() {
        val viewModel = viewModel()
        viewModel.onIntent(CartIntent.ToggleSelectAll)
        assertTrue(viewModel.uiState.allSelected)
        assertEquals(6, viewModel.uiState.selectedCount)
        // 全选合计 = 2500 + 880 + 19900 + 12900 + 11970 + 2500 = 50650
        assertEquals(50650, viewModel.uiState.totalCents)

        viewModel.onIntent(CartIntent.ToggleSelectAll)
        assertFalse(viewModel.uiState.allSelected)
        assertEquals(0, viewModel.uiState.selectedCount)
        assertEquals(0, viewModel.uiState.totalCents)
    }
}
