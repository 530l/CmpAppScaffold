package com.lyf.cmp.core.ui.loadmore

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 下拉刷新 + 触底加载的列表容器：
 * - 刷新用 Material3 官方 [PullToRefreshBox]（common 源集，iOS/Android 行为一致）；
 * - footer 自动追加在 [content] 之后，feature 不需要自己记得加；
 * - 触底检测内置（[LoadMoreTrigger]），去重交给 [LoadableController]。
 *
 * footer 外观可通过 [footerContent] 整体替换。
 */
@Composable
fun LoadableLazyColumn(
    isRefreshing: Boolean,
    loadMoreState: LoadMoreState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    loadMoreThreshold: Int = DEFAULT_LOAD_MORE_THRESHOLD,
    showEndFooter: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    userScrollEnabled: Boolean = true,
    footerContent: @Composable (LoadMoreState) -> Unit = { loadState ->
        LoadMoreFooter(
            state = loadState,
            onRetry = onLoadMore,
            showEndText = showEndFooter,
        )
    },
    content: LazyListScope.() -> Unit,
) {
    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            horizontalAlignment = horizontalAlignment,
            userScrollEnabled = userScrollEnabled,
        ) {
            content()
            item(key = LOAD_MORE_FOOTER_KEY) { footerContent(loadMoreState) }
        }
        LoadMoreTrigger(
            state = state,
            loadMoreState = loadMoreState,
            onLoadMore = onLoadMore,
            threshold = loadMoreThreshold,
        )
    }
}

private const val LOAD_MORE_FOOTER_KEY = "load_more_footer"
