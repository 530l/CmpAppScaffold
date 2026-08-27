package com.lyf.cmp.feature.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyf.cmp.core.log.AppLogger
import com.lyf.cmp.core.model.Money
import com.lyf.cmp.core.ui.loadmore.LoadableController
import com.lyf.cmp.core.ui.loadmore.LoadableUiState
import com.lyf.cmp.core.ui.loadmore.LoadMoreState
import com.lyf.cmp.core.ui.loadmore.Page
import com.lyf.cmp.feature.cart.data.ArticleRepository
import com.lyf.cmp.feature.cart.domain.Article
import kotlinx.coroutines.flow.StateFlow

/**
 * 演示价规则：列表第 position 条（0 基）定价 (position + 1) 分，
 * 底栏合计为已选条目的位置累加。仅用于脚手架演示金额链路，无业务含义。
 */
fun demoUnitPrice(position: Int): Money = Money((position + 1).toLong())

data class CartUiState(
    override val dataList: List<Article> = emptyList(),
    override val isRefreshing: Boolean = false,
    override val isInitializing: Boolean = true,
    override val loadMoreState: LoadMoreState = LoadMoreState.Idle,
    val error: CartError? = null,
) : LoadableUiState<Article, CartUiState> {
    val allSelected: Boolean get() = dataList.isNotEmpty() && dataList.all(Article::selected)
    val selectedCount: Int get() = dataList.count(Article::selected)
    val total: Money
        get() = dataList.withIndex()
            .filter { (_, item) -> item.selected }
            .fold(Money.zero()) { acc, (index, _) -> acc + demoUnitPrice(index) }

    override fun copyState(
        dataList: List<Article>,
        isRefreshing: Boolean,
        isInitializing: Boolean,
        loadMoreState: LoadMoreState,
    ): CartUiState = copy(
        dataList = dataList,
        isRefreshing = isRefreshing,
        isInitializing = isInitializing,
        loadMoreState = loadMoreState,
    )
}

enum class CartError {
    LOAD_FAILED,
}

sealed interface CartIntent {
    data class ToggleItem(val itemId: Long) : CartIntent
    data object ToggleSelectAll : CartIntent
    data object Refresh : CartIntent
    data object LoadMore : CartIntent
    data object Retry : CartIntent
}

class CartViewModel(
    private val repository: ArticleRepository,
) : ViewModel() {
    private val loadable: LoadableController<Article, CartUiState> =
        LoadableController(
            scope = viewModelScope,
            initialUiState = CartUiState(),
            loadPage = ::loadPage,
            onError = ::onLoadError,
        )

    val uiState: StateFlow<CartUiState> = loadable.uiState

    init {
        loadable.initialize()
    }

    /** 加载成功即清除旧错误（banner 随下一次成功消失）。 */
    private suspend fun loadPage(page: Int): Result<Page<Article>> =
        repository.loadPage(page).onSuccess {
            loadable.updateState { state -> state.copy(error = null) }
        }

    private fun onLoadError(error: Throwable, @Suppress("UNUSED_PARAMETER") isListEmpty: Boolean) {
        AppLogger.error(TAG, error) { "文章列表加载失败" }
        loadable.updateState { state -> state.copy(error = CartError.LOAD_FAILED) }
    }

    fun onIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.ToggleItem -> loadable.updateState { state ->
                state.copy(
                    dataList = state.dataList.map {
                        if (it.id == intent.itemId) it.copy(selected = !it.selected) else it
                    },
                )
            }

            CartIntent.ToggleSelectAll -> loadable.updateState { state ->
                val target = !state.allSelected
                state.copy(dataList = state.dataList.map { it.copy(selected = target) })
            }

            CartIntent.Refresh -> loadable.refresh()
            CartIntent.LoadMore -> loadable.loadMore()
            CartIntent.Retry -> loadable.initialize()
        }
    }

    private companion object {
        const val TAG = "CartViewModel"
    }
}
