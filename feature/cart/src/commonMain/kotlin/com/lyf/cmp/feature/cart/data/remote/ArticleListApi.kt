package com.lyf.cmp.feature.cart.data.remote

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse

/**
 * wanandroid 文章列表，脚手架演示数据源（只接这一个接口）。
 * 页码 0 起：`article/list/0/json` 为第一页。
 */
interface ArticleListApi {
    @GET("article/list/{page}/json")
    suspend fun getArticleList(@Path("page") page: Int): HttpResponse
}
