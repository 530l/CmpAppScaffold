package com.lyf.cmpdemo.feature.cart

import com.lyf.cmpdemo.feature.cart.data.CartRepository
import com.lyf.cmpdemo.feature.cart.data.MockCartRepository
import com.lyf.cmpdemo.feature.cart.presentation.CartViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// feature 级 Koin module：新增 feature 时各自带 module，
// 并在 SharedAppInitializer 的 startKoin 中注册
val cartModule = module {
    single<CartRepository> { MockCartRepository() }
    viewModel { CartViewModel(get()) }
}
