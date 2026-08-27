package com.lyf.cmp.feature.cart.data.remote

import com.lyf.cmp.core.network.NetworkResult
import com.lyf.cmp.core.network.safeRequest

/** API 接口与 Repository 之间的边界，统一在这里完成网络错误转换。 */
class ArticleRemoteDataSource(
    private val api: ArticleListApi,
) {
    suspend fun getArticleList(page: Int): NetworkResult<WanResponse<WanArticleListDto>> =
        safeRequest { api.getArticleList(page) }
}
