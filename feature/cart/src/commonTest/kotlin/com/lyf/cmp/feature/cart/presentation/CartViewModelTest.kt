package com.lyf.cmp.feature.cart.presentation

import com.lyf.cmp.core.model.Money
import com.lyf.cmp.core.ui.loadmore.LoadMoreState
import com.lyf.cmp.core.ui.loadmore.Page
import com.lyf.cmp.core.util.formatMoney
import com.lyf.cmp.feature.cart.data.ArticleRepository
import com.lyf.cmp.feature.cart.domain.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    fun initialLoadShowsFirstPage() = runTest(dispatcher) {
        val repository = FakeArticleRepository { page -> successPage(page) }
        val viewModel = CartViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.dataList.size)
        assertFalse(state.isInitializing)
        assertFalse(state.isRefreshing)
        assertNull(state.error)
        assertEquals(listOf(1), repository.requestedPages)
    }

    @Test
    fun initialFailureShowsErrorThenRetryRecovers() = runTest(dispatcher) {
        val repository = FakeArticleRepository { page ->
            if (page == 1 && requestedPages.size == 1) {
                Result.failure(IllegalStateException("网络连接失败"))
            } else {
                successPage(page)
            }
        }
        val viewModel = CartViewModel(repository)
        advanceUntilIdle()

        assertEquals(CartError.LOAD_FAILED, viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.dataList.isEmpty())

        viewModel.onIntent(CartIntent.Retry)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(3, viewModel.uiState.value.dataList.size)
        assertEquals(listOf(1, 1), repository.requestedPages)
    }

    @Test
    fun positionBasedTotalsAndSelectAll() = runTest(dispatcher) {
        val viewModel = CartViewModel(FakeArticleRepository { page -> successPage(page) })
        advanceUntilIdle()

        // 演示价 = position + 1（分）：选中第 0、1 条合计 1 + 2 = 3 分。
        viewModel.onIntent(CartIntent.ToggleItem(1))
        viewModel.onIntent(CartIntent.ToggleItem(2))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.selectedCount)
        assertEquals(Money(3), viewModel.uiState.value.total)
        assertFalse(viewModel.uiState.value.allSelected)

        viewModel.onIntent(CartIntent.ToggleSelectAll)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.allSelected)
        assertEquals(3, viewModel.uiState.value.selectedCount)
        assertEquals(Money(6), viewModel.uiState.value.total)

        viewModel.onIntent(CartIntent.ToggleSelectAll)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.allSelected)
        assertEquals(Money.zero(), viewModel.uiState.value.total)
    }

    @Test
    fun refreshFailureKeepsItemsAndShowsError() = runTest(dispatcher) {
        val repository = FakeArticleRepository { page ->
            if (requestedPages.size == 1) successPage(page)
            else Result.failure(IllegalStateException("网络连接失败"))
        }
        val viewModel = CartViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(CartIntent.Refresh)
        advanceUntilIdle()

        assertEquals(CartError.LOAD_FAILED, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(3, viewModel.uiState.value.dataList.size)
    }

    @Test
    fun refreshSuccessClearsStaleError() = runTest(dispatcher) {
        val repository = FakeArticleRepository { page ->
            if (requestedPages.size == 1) {
                Result.failure(IllegalStateException("网络连接失败"))
            } else {
                successPage(page)
            }
        }
        val viewModel = CartViewModel(repository)
        advanceUntilIdle()
        assertEquals(CartError.LOAD_FAILED, viewModel.uiState.value.error)

        viewModel.onIntent(CartIntent.Refresh)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(3, viewModel.uiState.value.dataList.size)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun loadMoreAppendsNextPageThenStopsAtEnd() = runTest(dispatcher) {
        val repository = FakeArticleRepository { page -> successPage(page) }
        val viewModel = CartViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(CartIntent.LoadMore)
        advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.dataList.size)
        assertEquals(LoadMoreState.End, viewModel.uiState.value.loadMoreState)
        assertEquals(listOf(1, 2), repository.requestedPages)

        // 已到最后一页，继续触底短路，不再发请求。
        viewModel.onIntent(CartIntent.LoadMore)
        advanceUntilIdle()
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    /** 页码 p 返回 3 条数据，第 2 页起 hasMore=false。 */
    private fun successPage(page: Int): Result<Page<Article>> = Result.success(
        Page(
            items = (1..3).map { offset ->
                val id = ((page - 1) * 3 + offset).toLong()
                Article(
                    id = id,
                    title = "文章 $id",
                    author = "作者",
                    chapterName = "体系课程",
                    link = "https://www.wanandroid.com/article/$id",
                    niceDate = "1 小时前",
                )
            },
            hasMore = page < 2,
        ),
    )
}

private class FakeArticleRepository(
    private val handler: FakeArticleRepository.(page: Int) -> Result<Page<Article>>,
) : ArticleRepository {
    val requestedPages = mutableListOf<Int>()

    override suspend fun loadPage(page: Int): Result<Page<Article>> {
        requestedPages += page
        return handler(page)
    }
}
