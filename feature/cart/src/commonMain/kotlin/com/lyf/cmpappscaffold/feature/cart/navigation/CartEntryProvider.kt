package com.lyf.cmpappscaffold.feature.cart.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lyf.cmpappscaffold.feature.cart.presentation.CartScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** 购物车模块拥有自己的路由集合，其他 Feature 不直接引用其页面实现。 */
@Serializable
sealed interface CartRoute : NavKey {
    @Serializable
    data object Main : CartRoute
}

val cartNavigationSerializers = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(CartRoute.Main::class, CartRoute.Main.serializer())
    }
}

fun EntryProviderScope<NavKey>.cartEntryProvider(
    onCheckout: (selectedItemIds: List<Long>) -> Unit,
) {
    entry<CartRoute.Main> {
        CartScreen(onCheckout = onCheckout)
    }
}
