package com.lyf.cmpdemo.cart

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ViewModel 核心行为回归
class CartViewModelTest {

    @Test
    fun formatPriceConvertsCentsToYuanString() {
        assertEquals("¥0.00", formatPrice(0))
        assertEquals("¥0.05", formatPrice(5))
        assertEquals("¥33.80", formatPrice(3380))
        assertEquals("¥125.00", formatPrice(12500))
    }

    @Test
    fun totalCentsSumsOnlySelectedItems() {
        val viewModel = CartViewModel()
        viewModel.toggleItem(viewModel.items[0]) // 挂耳咖啡 1250 × 2 = 2500
        viewModel.toggleItem(viewModel.items[1]) // 马克杯 880 × 1 = 880
        assertEquals(2, viewModel.selectedCount)
        assertEquals(3380, viewModel.totalCents)
        assertEquals("¥33.80", formatPrice(viewModel.totalCents))
        assertFalse(viewModel.allSelected)
    }

    @Test
    fun toggleSelectAllFromPartialSelectionSelectsAll() {
        val viewModel = CartViewModel()
        viewModel.toggleItem(viewModel.items[0]) // 1/6 选中：半选态
        assertFalse(viewModel.allSelected)

        viewModel.toggleSelectAll() // 半选态应走向全选，而不是清空
        assertTrue(viewModel.allSelected)
        assertEquals(6, viewModel.selectedCount)
    }

    @Test
    fun toggleSelectAllSwitchesBetweenAllAndNone() {
        val viewModel = CartViewModel()
        viewModel.toggleSelectAll()
        assertTrue(viewModel.allSelected)
        assertEquals(6, viewModel.selectedCount)
        // 全选合计 = 2500 + 880 + 19900 + 12900 + 11970 + 2500 = 50650
        assertEquals(50650, viewModel.totalCents)

        viewModel.toggleSelectAll()
        assertFalse(viewModel.allSelected)
        assertEquals(0, viewModel.selectedCount)
        assertEquals(0, viewModel.totalCents)
    }
}
