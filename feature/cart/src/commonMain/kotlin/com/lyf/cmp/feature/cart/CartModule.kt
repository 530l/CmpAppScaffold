package com.lyf.cmp.feature.cart

import com.lyf.cmp.feature.cart.data.ArticleRepository
import com.lyf.cmp.feature.cart.data.DefaultArticleRepository
import com.lyf.cmp.feature.cart.data.remote.ArticleRemoteDataSource
import com.lyf.cmp.feature.cart.data.remote.createArticleListApi
import com.lyf.cmp.feature.cart.presentation.CartViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cartModule = module {
    single { get<Ktorfit>().createArticleListApi() }
    single { ArticleRemoteDataSource(get()) }
    single<ArticleRepository> { DefaultArticleRepository(get()) }
    viewModel { CartViewModel(get()) }
}
