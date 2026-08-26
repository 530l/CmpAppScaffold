package com.lyf.cmp.feature.cart.presentation

import com.lyf.cmp.core.model.Money
import com.lyf.cmp.core.network.NetworkError
import com.lyf.cmp.core.network.NetworkResult
import com.lyf.cmp.core.util.formatMoney
import com.lyf.cmp.feature.cart.data.CartRepository
import com.lyf.cmp.feature.cart.domain.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun formatMoneyUsesMinorUnits() {
        assertEquals("¥0.00", formatMoney(Money(0)))
        assertEquals("¥0.05", formatMoney(Money(5)))
        assertEquals("¥33.80", formatMoney(Money(3_380)))
    }

    @Test
    fun selectedItemsProduceCorrectQuantityAndTotal() = runTest(dispatcher) {
        val viewModel = CartViewModel(FakeCartRepository())
        advanceUntilIdle()

        viewModel.onIntent(CartIntent.ToggleItem(1))
        viewModel.onIntent(CartIntent.ToggleItem(2))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.selectedLineCount)
        assertEquals(3L, viewModel.uiState.value.selectedQuantity)
        assertEquals(Money(3_380), viewModel.uiState.value.total)
        assertFalse(viewModel.uiState.value.allSelected)
    }

    @Test
    fun toggleSelectAllSwitchesBetweenAllAndNone() = runTest(dispatcher) {
        val viewModel = CartViewModel(FakeCartRepository())
        advanceUntilIdle()

        viewModel.onIntent(CartIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.allSelected)
        assertEquals(6, viewModel.uiState.value.selectedLineCount)
        assertEquals(Money(50_650), viewModel.uiState.value.total)

        viewModel.onIntent(CartIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.allSelected)
        assertEquals(0, viewModel.uiState.value.selectedLineCount)
        assertEquals(Money.zero(), viewModel.uiState.value.total)
    }

    @Test
    fun refreshSuccessStopsIndicatorAndClearsError() = runTest(dispatcher) {
        val viewModel = CartViewModel(FakeCartRepository())
        advanceUntilIdle()

        viewModel.onIntent(CartIntent.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertNull(viewModel.uiState.value.error)
        assertEquals(6, viewModel.uiState.value.items.size)
    }

    @Test
    fun refreshFailureKeepsItemsAndShowsLoadError() = runTest(dispatcher) {
        val viewModel = CartViewModel(
            FakeCartRepository(
                refreshResult = NetworkResult.Failure(NetworkError.Connectivity(IOException())),
            ),
        )
        advanceUntilIdle()

        viewModel.onIntent(CartIntent.Refresh)
        advanceUntilIdle()

        assertEquals(CartError.LOAD_FAILED, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(6, viewModel.uiState.value.items.size)
    }
}

private class FakeCartRepository(
    private val refreshResult: NetworkResult<Unit> = NetworkResult.Success(Unit, 200),
) : CartRepository {
    private val items = MutableStateFlow(SEED_ITEMS)

    override fun observeCartItems(): Flow<List<CartItem>> = items

    override suspend fun ensureSeeded() = Unit

    override suspend fun refreshFromRemote(): NetworkResult<Unit> = refreshResult

    override suspend fun toggleSelection(itemId: Long) {
        items.value = items.value.map { item ->
            if (item.id == itemId) item.copy(selected = !item.selected) else item
        }
    }

    override suspend fun setAllSelected(selected: Boolean) {
        items.value = items.value.map { it.copy(selected = selected) }
    }
}

private val SEED_ITEMS = listOf(
    CartItem(1, "挂耳咖啡 · 10 包装", Money(1_250), 2, "☕"),
    CartItem(2, "陶瓷马克杯 380ml", Money(880), 1, "🍵"),
    CartItem(3, "无线蓝牙耳机 · 半入耳", Money(19_900), 1, "🎧"),
    CartItem(4, "快充充电宝 20000mAh", Money(12_900), 1, "🔋"),
    CartItem(5, "棉麻收纳袋 · 三件套", Money(3_990), 3, "🧺"),
    CartItem(6, "极简木质台历 2026", Money(2_500), 1, "📅"),
)
