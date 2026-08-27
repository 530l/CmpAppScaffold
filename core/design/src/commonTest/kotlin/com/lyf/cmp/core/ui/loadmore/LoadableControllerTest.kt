package com.lyf.cmp.core.ui.loadmore

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoadableControllerTest {

    @Test
    fun initializeLoadsFirstPageAndMarksEnd() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a", "b"), hasMore = false)) }
        }
        val controller = newController(loader = loader)

        controller.initialize()
        advanceUntilIdle()

        val state = controller.uiState.value
        assertEquals(listOf("a", "b"), state.dataList)
        assertFalse(state.isInitializing)
        assertFalse(state.isRefreshing)
        assertEquals(LoadMoreState.End, state.loadMoreState)
    }

    @Test
    fun loadMoreIgnoredAfterEnd() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = false)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        controller.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(1), loader.requestedPages)
    }

    @Test
    fun refreshReplacesDataAndResetsEnd() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = false)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()
        assertEquals(LoadMoreState.End, controller.uiState.value.loadMoreState)

        loader.handler = { Result.success(Page(listOf("x", "y"), hasMore = true)) }
        controller.refresh()
        advanceUntilIdle()

        val state = controller.uiState.value
        assertEquals(listOf("x", "y"), state.dataList)
        assertEquals(LoadMoreState.Idle, state.loadMoreState)

        loader.handler = { page -> Result.success(Page(listOf("p$page"), hasMore = false)) }
        controller.loadMore()
        advanceUntilIdle()
        assertEquals(listOf("x", "y", "p2"), controller.uiState.value.dataList)
        assertEquals(listOf(1, 1, 2), loader.requestedPages)
    }

    @Test
    fun loadMoreAppendsPagesUntilHasMoreIsFalse() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { page ->
                Result.success(Page(listOf("p$page"), hasMore = page < 3))
            }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        controller.loadMore()
        advanceUntilIdle()
        controller.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("p1", "p2", "p3"), controller.uiState.value.dataList)
        assertEquals(LoadMoreState.End, controller.uiState.value.loadMoreState)
        assertEquals(listOf(1, 2, 3), loader.requestedPages)
    }

    @Test
    fun loadMoreIgnoredWhileRefreshing() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = true)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        val refreshGate = CompletableDeferred<Unit>()
        loader.handler = { page ->
            refreshGate.await()
            Result.success(Page(listOf("b"), hasMore = true))
        }
        controller.refresh()
        advanceUntilIdle()
        assertTrue(controller.uiState.value.isRefreshing)

        controller.loadMore()
        advanceUntilIdle()
        refreshGate.complete(Unit)
        advanceUntilIdle()

        // 只有初始化与刷新各一次 page1 请求，loadMore 未发出请求。
        assertEquals(listOf(1, 1), loader.requestedPages)
        assertEquals(listOf("b"), controller.uiState.value.dataList)
    }

    @Test
    fun duplicateLoadMoreSendsSingleRequest() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = true)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        val loadGate = CompletableDeferred<Unit>()
        loader.handler = { page ->
            loadGate.await()
            Result.success(Page(listOf("b"), hasMore = true))
        }
        controller.loadMore()
        advanceUntilIdle()
        assertEquals(LoadMoreState.Loading, controller.uiState.value.loadMoreState)

        controller.loadMore()
        advanceUntilIdle()
        assertEquals(listOf(1, 2), loader.requestedPages)

        loadGate.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf("a", "b"), controller.uiState.value.dataList)
        assertEquals(LoadMoreState.Idle, controller.uiState.value.loadMoreState)
    }

    @Test
    fun loadMoreFailureShowsFailedAndRetrySucceeds() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = true)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        loader.handler = { Result.failure(IllegalStateException("boom")) }
        controller.loadMore()
        advanceUntilIdle()
        assertEquals(LoadMoreState.Failed, controller.uiState.value.loadMoreState)

        loader.handler = { Result.success(Page(listOf("b"), hasMore = false)) }
        controller.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("a", "b"), controller.uiState.value.dataList)
        assertEquals(LoadMoreState.End, controller.uiState.value.loadMoreState)
        // 失败重试重新加载失败的那一页，页码不前进。
        assertEquals(listOf(1, 2, 2), loader.requestedPages)
    }

    @Test
    fun refreshFailureKeepsListAndReportsNonEmptyError() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a", "b"), hasMore = false)) }
        }
        val errors = ErrorCaptor()
        val controller = newController(loader = loader, onError = errors::onError)
        controller.initialize()
        advanceUntilIdle()

        val failure: Throwable = IllegalStateException("offline")
        loader.handler = { Result.failure(failure) }
        controller.refresh()
        advanceUntilIdle()

        assertEquals(listOf("a", "b"), controller.uiState.value.dataList)
        assertFalse(controller.uiState.value.isRefreshing)
        assertEquals(listOf(failure to false), errors.recorded)
    }

    @Test
    fun emptyLoadMorePageMarksEndToAvoidLoop() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = true)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        loader.handler = { Result.success(Page(emptyList(), hasMore = true)) }
        controller.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("a"), controller.uiState.value.dataList)
        assertEquals(LoadMoreState.End, controller.uiState.value.loadMoreState)

        controller.loadMore()
        advanceUntilIdle()
        assertEquals(listOf(1, 2), loader.requestedPages)
    }

    @Test
    fun localDataShowsBeforeServerResultArrives() = runTest {
        val loader = ScriptedLoader()
        val serverGate = CompletableDeferred<Unit>()
        loader.handler = {
            serverGate.await()
            Result.success(Page(listOf("srv"), hasMore = false))
        }
        val controller = newController(loader = loader, localData = { listOf("local") })

        controller.initialize()
        advanceUntilIdle()

        assertEquals(listOf("local"), controller.uiState.value.dataList)
        assertFalse(controller.uiState.value.isInitializing)

        serverGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf("srv"), controller.uiState.value.dataList)
        assertFalse(controller.uiState.value.isInitializing)
    }

    @Test
    fun loadMoreIgnoredDuringSilentRefresh() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = true)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        val refreshGate = CompletableDeferred<Unit>()
        loader.handler = { _ ->
            refreshGate.await()
            Result.success(Page(listOf("b"), hasMore = true))
        }
        controller.refresh(silent = true)
        advanceUntilIdle()
        // silent 刷新不亮指示器，但首页请求已在途。
        assertFalse(controller.uiState.value.isRefreshing)

        controller.loadMore()
        advanceUntilIdle()
        refreshGate.complete(Unit)
        advanceUntilIdle()

        // 静默刷新期间触底加载被忽略，未发出 page2 请求。
        assertEquals(listOf(1, 1), loader.requestedPages)
        assertEquals(listOf("b"), controller.uiState.value.dataList)
    }

    @Test
    fun repeatedSilentRefreshSendsSingleRequest() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("a"), hasMore = true)) }
        }
        val controller = newController(loader = loader)
        controller.initialize()
        advanceUntilIdle()

        val refreshGate = CompletableDeferred<Unit>()
        loader.handler = { _ ->
            refreshGate.await()
            Result.success(Page(listOf("b"), hasMore = true))
        }
        controller.refresh(silent = true)
        advanceUntilIdle()
        controller.refresh(silent = true)
        advanceUntilIdle()
        refreshGate.complete(Unit)
        advanceUntilIdle()

        // 在途 silent 刷新挡住后续刷新，只多发一次首页请求。
        assertEquals(listOf(1, 1), loader.requestedPages)
        assertEquals(listOf("b"), controller.uiState.value.dataList)
    }

    @Test
    fun loadMoreIgnoredWhileLocalDataShownBeforeServerArrives() = runTest {
        val loader = ScriptedLoader()
        val serverGate = CompletableDeferred<Unit>()
        loader.handler = { _ ->
            serverGate.await()
            Result.success(Page(listOf("srv"), hasMore = true))
        }
        val controller = newController(loader = loader, localData = { listOf("local") })

        controller.initialize()
        advanceUntilIdle()
        // 本地数据已展示、initializing 已提前结束，但首页请求仍在途。
        assertFalse(controller.uiState.value.isInitializing)
        assertEquals(listOf("local"), controller.uiState.value.dataList)

        controller.loadMore()
        advanceUntilIdle()
        serverGate.complete(Unit)
        advanceUntilIdle()

        // 触底加载未发出 page2 请求，首页到达后整页替换本地数据。
        assertEquals(listOf(1), loader.requestedPages)
        assertEquals(listOf("srv"), controller.uiState.value.dataList)
    }

    @Test
    fun localDataFailureFallsThroughToRemote() = runTest {
        val loader = ScriptedLoader().apply {
            handler = { Result.success(Page(listOf("srv"), hasMore = false)) }
        }
        val controller = newController(
            loader = loader,
            localData = { throw IllegalStateException("本地数据损坏") },
        )

        controller.initialize()
        advanceUntilIdle()

        // 本地数据失败静默降级，远端结果正常落地。
        val state = controller.uiState.value
        assertEquals(listOf("srv"), state.dataList)
        assertFalse(state.isInitializing)
    }

    @Test
    fun initializeFailureWithEmptyListReportsEmptyError() = runTest {
        val failure: Throwable = IllegalStateException("no network")
        val loader = ScriptedLoader().apply {
            handler = { Result.failure(failure) }
        }
        val errors = ErrorCaptor()
        val controller = newController(loader = loader, onError = errors::onError)

        controller.initialize()
        advanceUntilIdle()

        val state = controller.uiState.value
        assertTrue(state.dataList.isEmpty())
        assertFalse(state.isInitializing)
        assertEquals(listOf(failure to true), errors.recorded)
    }

    private fun TestScope.newController(
        loader: ScriptedLoader,
        localData: (suspend () -> List<String>)? = null,
        onError: ((Throwable, Boolean) -> Unit)? = null,
    ): LoadableController<String, TestUiState> = LoadableController(
        scope = this,
        initialUiState = TestUiState(),
        loadPage = loader::load,
        localData = localData,
        onError = onError,
    )
}

private data class TestUiState(
    override val dataList: List<String> = emptyList(),
    override val isRefreshing: Boolean = false,
    override val isInitializing: Boolean = true,
    override val loadMoreState: LoadMoreState = LoadMoreState.Idle,
) : LoadableUiState<String, TestUiState> {

    override fun copyState(
        dataList: List<String>,
        isRefreshing: Boolean,
        isInitializing: Boolean,
        loadMoreState: LoadMoreState,
    ): TestUiState = copy(
        dataList = dataList,
        isRefreshing = isRefreshing,
        isInitializing = isInitializing,
        loadMoreState = loadMoreState,
    )
}

private class ScriptedLoader {
    val requestedPages = mutableListOf<Int>()
    var handler: suspend (Int) -> Result<Page<String>> =
        { Result.success(Page(items = emptyList<String>(), hasMore = false)) }

    suspend fun load(page: Int): Result<Page<String>> {
        requestedPages += page
        return handler(page)
    }
}

private class ErrorCaptor {
    val recorded = mutableListOf<Pair<Throwable, Boolean>>()

    fun onError(error: Throwable, isListEmpty: Boolean) {
        recorded += error to isListEmpty
    }
}
