package com.lyf.cmp.core.ui.loadmore

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** iOS 缩短触发距离，避免 Material3 默认拖动距离过长导致刷新不易触发。 */
internal actual val platformPullToRefreshThreshold: Dp = 48.dp

/** iOS 使用接近 UIRefreshControl 的紧凑进度环。 */
internal actual val platformUsesCompactPullToRefreshIndicator: Boolean = true

/** iOS 使用短动画刷新状态，避免复位期间长时间拒绝下一次手势。 */
@Composable
internal actual fun rememberPlatformPullToRefreshState(): PullToRefreshState =
    remember { IosPullToRefreshState() }

/**
 * iOS 分页列表容器：
 * - 内容跟随下拉位移下移（最大一个阈值位），刷新环落在留出的空隙里，接近原生手感；
 * - 末端保留阻尼橡胶带回弹，恢复 iOS 列表底部的自然边界；
 * - 顶端回弹仍然关闭，否则 Cupertino 回弹会吃掉下拉手势的剩余位移，刷新无法触发。
 * 位移全部走 draw 层 graphicsLayer，不触发布局与重组。
 */
@Composable
internal actual fun PlatformListContainer(
    pullToRefreshState: PullToRefreshState,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    // 内容位移 = 触发阈值 + 呼吸空隙：指示环与首行内容之间始终留出空隙，不贴边。
    val contentFollowMaxPx = remember(density) {
        with(density) { (platformPullToRefreshThreshold + REFRESH_HEADER_GAP).toPx() }
    }
    val bounce = remember(density, scope) {
        BottomBounceState(
            maxOffsetPx = with(density) { MAX_BOUNCE_OFFSET.toPx() },
            scope = scope,
        )
    }
    CompositionLocalProvider(LocalOverscrollFactory provides null) {
        Box(
            modifier = Modifier
                .clipToBounds()
                .graphicsLayer {
                    translationY =
                        pullToRefreshState.distanceFraction.coerceIn(0f, 1f) * contentFollowMaxPx +
                        bounce.offset
                }
                .nestedScroll(remember(bounce) { BottomBounceConnection(bounce) }),
        ) {
            content()
        }
    }
}

@Stable
private class IosPullToRefreshState : PullToRefreshState {
    private val fraction = mutableFloatStateOf(0f)
    private var animationJob: Job? = null

    override val distanceFraction: Float
        get() = fraction.floatValue

    override val isAnimating: Boolean
        get() = animationJob?.isActive == true

    override suspend fun animateToThreshold() = animateTo(1f)

    override suspend fun animateToHidden() = animateTo(0f)

    /** 手势驱动路径直写快照状态：无 Animatable 锁与挂起开销，指示环与内容紧跟手指。 */
    override suspend fun snapTo(targetValue: Float) {
        animationJob?.cancel()
        fraction.floatValue = targetValue.coerceIn(0f, IOS_MAX_PULL_FRACTION)
    }

    /** 单一动画 Job 串行化，后发动画取消先行动画，避免两个动画协程互相覆盖。 */
    private suspend fun animateTo(targetValue: Float) {
        animationJob?.cancel()
        val from = fraction.floatValue
        if (from == targetValue) return
        coroutineScope {
            val job = launch {
                animate(
                    initialValue = from,
                    targetValue = targetValue,
                    animationSpec = tween(
                        durationMillis = IOS_PULL_ANIMATION_DURATION_MILLIS,
                        easing = FastOutSlowInEasing,
                    ),
                ) { value, _ -> fraction.floatValue = value }
            }
            animationJob = job
            job.join()
        }
    }
}

/** 末端橡胶带：位移恒 ≤ 0，收拢/释放均可被新手势立即接管。 */
@Stable
private class BottomBounceState(
    private val maxOffsetPx: Float,
    private val scope: CoroutineScope,
) {
    var offset by mutableFloatStateOf(0f)
        private set

    val isActive: Boolean get() = offset != 0f

    /** 向末端拖拽越界，按阻尼累积；返回消耗掉的滚动 delta。 */
    fun drag(deltaPx: Float): Float {
        releaseJob?.cancel()
        val result = EdgePhysics.drag(offset, deltaPx, maxOffsetPx, DRAG_DAMPING)
        offset = result.offsetPx
        return result.consumedDeltaPx
    }

    /** 反向拖拽 1:1 收拢；返回消耗掉的滚动 delta。 */
    fun collapse(deltaPx: Float): Float {
        if (!isActive) return 0f
        releaseJob?.cancel()
        val result = EdgePhysics.collapse(offset, deltaPx)
        offset = result.offsetPx
        return result.consumedDeltaPx
    }

    /** fling 剩余速度折算成一次越界冲量。 */
    fun absorbVelocity(velocityPx: Float) {
        offset = EdgePhysics.kick(offset, velocityPx, maxOffsetPx, VELOCITY_TO_OFFSET)
    }

    /** 松手后的回弹收拢动画；新拖拽会取消它。 */
    fun release() {
        if (!isActive) return
        releaseJob?.cancel()
        releaseJob = scope.launch {
            animate(
                initialValue = offset,
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = RELEASE_DAMPING_RATIO,
                    stiffness = RELEASE_STIFFNESS,
                ),
            ) { value, _ -> offset = value }
        }
    }

    private var releaseJob: Job? = null
}

/**
 * 列表末端之外的滚动/抛掷事件收进橡胶带；顶端不拦截，留给下拉刷新。
 */
private class BottomBounceConnection(
    private val bounce: BottomBounceState,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = when {
        source != NestedScrollSource.UserInput -> Offset.Zero
        // 回弹未收拢时，反向滚动先 1:1 收拢回弹，再让列表自己滚动。
        available.y > 0f -> Offset(0f, bounce.collapse(available.y))
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = when {
        source != NestedScrollSource.UserInput -> Offset.Zero
        // 列表到末端后仍有向末端的 delta：吃进橡胶带。
        available.y < 0f -> Offset(0f, bounce.drag(available.y))
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (!bounce.isActive) return Velocity.Zero
        if (available.y < 0f) {
            // 列表已在末端，子组件本就消费不了向末端速度，全部转成冲量。
            bounce.absorbVelocity(available.y)
            bounce.release()
            return available
        }
        bounce.release()
        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (available.y >= 0f) return Velocity.Zero
        // fling 冲到末端后的剩余速度转为一次越界冲量再收拢。
        bounce.absorbVelocity(available.y)
        bounce.release()
        return available
    }
}

private const val IOS_PULL_ANIMATION_DURATION_MILLIS = 140
private const val IOS_MAX_PULL_FRACTION = 1.25f

/** 刷新指示环与列表首行之间的呼吸空隙。 */
private val REFRESH_HEADER_GAP = 12.dp

private const val DRAG_DAMPING = 0.5f
private const val VELOCITY_TO_OFFSET = 0.05f
private const val RELEASE_DAMPING_RATIO = 0.8f
private const val RELEASE_STIFFNESS = 350f
private val MAX_BOUNCE_OFFSET = 96.dp
