package com.lyf.cmp.core.ui.loadmore

import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/** 各平台默认的下拉刷新触发阈值。 */
internal expect val platformPullToRefreshThreshold: Dp

/** 是否使用紧凑、无容器背景的刷新指示器。 */
internal expect val platformUsesCompactPullToRefreshIndicator: Boolean

/** 创建符合平台交互节奏的刷新状态。 */
@Composable
internal expect fun rememberPlatformPullToRefreshState(): PullToRefreshState

/**
 * 分页列表的平台容器：Android 不做包装、保持官方滚动行为；
 * iOS 在此承载内容跟手位移、底部橡胶带回弹，并关闭与刷新手势竞争的边界回弹。
 */
@Composable
internal expect fun PlatformListContainer(
    pullToRefreshState: PullToRefreshState,
    content: @Composable () -> Unit,
)
