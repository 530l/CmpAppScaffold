package com.lyf.cmpappscaffold.feature.cart

import com.lyf.cmpappscaffold.feature.cart.data.CartRepository
import com.lyf.cmpappscaffold.feature.cart.data.DefaultCartRepository
import com.lyf.cmpappscaffold.feature.cart.data.remote.CartRemoteDataSource
import com.lyf.cmpappscaffold.feature.cart.data.remote.createCartApi
import com.lyf.cmpappscaffold.feature.cart.presentation.CartViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cartModule = module {
    single { get<Ktorfit>().createCartApi() }
    single { CartRemoteDataSource(get()) }
    single<CartRepository> { DefaultCartRepository(get()) }
    viewModel { CartViewModel(get()) }
}
