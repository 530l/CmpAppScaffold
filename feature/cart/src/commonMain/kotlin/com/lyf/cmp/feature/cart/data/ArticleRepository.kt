package com.lyf.cmp.feature.cart.data

import com.lyf.cmp.core.network.NetworkError
import com.lyf.cmp.core.network.NetworkResult
import com.lyf.cmp.core.ui.loadmore.LoadableController
import com.lyf.cmp.core.ui.loadmore.Page
import com.lyf.cmp.feature.cart.data.remote.ArticleRemoteDataSource
import com.lyf.cmp.feature.cart.data.remote.WanArticleListDto
import com.lyf.cmp.feature.cart.data.remote.WanResponse
import com.lyf.cmp.feature.cart.domain.Article

interface ArticleRepository {
    /** [page] 为 1 基页码（[LoadableController] 约定），内部映射为 wanandroid 的 0 基页码。 */
    suspend fun loadPage(page: Int): Result<Page<Article>>
}

class DefaultArticleRepository(
    private val dataSource: ArticleRemoteDataSource,
) : ArticleRepository {
    override suspend fun loadPage(page: Int): Result<Page<Article>> =
        when (val result = dataSource.getArticleList(page - LoadableController.FIRST_PAGE)) {
            is NetworkResult.Success -> result.value.toPageResult()
            is NetworkResult.Failure -> Result.failure(
                IllegalStateException(result.error.logMessage()),
            )
        }
}

private fun WanResponse<WanArticleListDto>.toPageResult(): Result<Page<Article>> {
    if (errorCode != 0) {
        return Result.failure(
            IllegalStateException("wanandroid errorCode=$errorCode ${errorMsg.orEmpty()}"),
        )
    }
    val list = data ?: return Result.failure(IllegalStateException("wanandroid 响应缺少 data"))

    return Result.success(
        Page(
            items = list.datas.map { dto ->
                Article(
                    id = dto.id,
                    title = dto.title,
                    author = dto.author.ifBlank { dto.shareUser },
                    chapterName = dto.chapterName.ifBlank { dto.superChapterName },
                    link = dto.link,
                    niceDate = dto.niceDate,
                )
            },
            // over=true 即最后一页；空页双保险交给 controller 的 End 判定。
            hasMore = !list.over && list.datas.isNotEmpty(),
        ),
    )
}

/** 仅供日志使用的错误摘要，界面文案走各 feature 的资源。 */
private fun NetworkError.logMessage(): String = when (this) {
    is NetworkError.Http -> "HTTP $statusCode"
    is NetworkError.Connectivity -> "网络连接失败"
    is NetworkError.InvalidPayload -> "响应解析失败"
    is NetworkError.Unknown -> "未知错误"
}
