package com.lyf.cmp.feature.cart.domain

/**
 * 文章条目（wanandroid 文章列表）。
 * wanandroid 的 author 与 shareUser 互补（站点文章取 author、用户分享取 shareUser），
 * 映射时统一收敛到 [author]。
 */
data class Article(
    val id: Long,
    val title: String,
    val author: String,
    val chapterName: String,
    val link: String,
    val niceDate: String,
    val selected: Boolean = false,
)
