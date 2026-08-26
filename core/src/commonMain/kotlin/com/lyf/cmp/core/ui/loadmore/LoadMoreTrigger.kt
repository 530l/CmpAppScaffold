package com.lyf.cmp.core.ui.loadmore

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow

/** 距底部剩余可见项数达到该值即预加载下一页。 */
const val DEFAULT_LOAD_MORE_THRESHOLD = 3

/**
 * 触底加载检测。只在 [LoadMoreState.Idle] 时触发：
 * Loading 由状态机去重、Failed 不自动重试（等用户点击 footer）、End 直接收口。
 * `totalItemsCount > threshold` 保证首屏不满一屏的短列表不会误触发。
 */
@Composable
fun LoadMoreTrigger(
    state: LazyListState,
    loadMoreState: LoadMoreState,
    onLoadMore: () -> Unit,
    threshold: Int = DEFAULT_LOAD_MORE_THRESHOLD,
) {
    val currentLoadMoreState by rememberUpdatedState(loadMoreState)
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val shouldLoadMore by remember(threshold) {
        derivedStateOf {
            val info = state.layoutInfo
            val lastVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val remainCount = info.totalItemsCount - lastVisibleIndex - 1
            info.totalItemsCount > threshold &&
                remainCount <= threshold &&
                currentLoadMoreState == LoadMoreState.Idle
        }
    }
    LaunchedEffect(state, threshold) {
        snapshotFlow { shouldLoadMore }.collect { if (it) currentOnLoadMore() }
    }
}

/** [LoadMoreTrigger] 的网格列表版本。 */
@Composable
fun LoadMoreTrigger(
    state: LazyGridState,
    loadMoreState: LoadMoreState,
    onLoadMore: () -> Unit,
    threshold: Int = DEFAULT_LOAD_MORE_THRESHOLD,
) {
    val currentLoadMoreState by rememberUpdatedState(loadMoreState)
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val shouldLoadMore by remember(threshold) {
        derivedStateOf {
            val info = state.layoutInfo
            val lastVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val remainCount = info.totalItemsCount - lastVisibleIndex - 1
            info.totalItemsCount > threshold &&
                remainCount <= threshold &&
                currentLoadMoreState == LoadMoreState.Idle
        }
    }
    LaunchedEffect(state, threshold) {
        snapshotFlow { shouldLoadMore }.collect { if (it) currentOnLoadMore() }
    }
}
