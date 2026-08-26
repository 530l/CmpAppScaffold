package com.lyf.cmpdemo.feature.cart

import com.lyf.cmpdemo.feature.cart.data.CartRepository
import com.lyf.cmpdemo.feature.cart.data.DefaultCartRepository
import com.lyf.cmpdemo.feature.cart.data.remote.CartRemoteDataSource
import com.lyf.cmpdemo.feature.cart.data.remote.createCartApi
import com.lyf.cmpdemo.feature.cart.presentation.CartViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cartModule = module {
    single { get<Ktorfit>().createCartApi() }
    single { CartRemoteDataSource(get()) }
    single<CartRepository> { DefaultCartRepository(get()) }
    viewModel { CartViewModel(get()) }
}
