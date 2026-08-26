package com.lyf.cmp.navigation

import androidx.compose.runtime.Composable
import com.lyf.cmp.core.navigation.AppNavHost
import com.lyf.cmp.feature.cart.navigation.CartRoute
import com.lyf.cmp.feature.cart.navigation.cartEntryProvider
import com.lyf.cmp.feature.cart.navigation.cartNavigationSerializers
import com.lyf.cmp.feature.login.navigation.LoginRoute
import com.lyf.cmp.feature.login.navigation.loginEntryProvider
import com.lyf.cmp.feature.login.navigation.loginNavigationSerializers
import kotlinx.serialization.modules.plus

private val appNavigationSerializers =
    cartNavigationSerializers + loginNavigationSerializers

/** 应用组合层只负责连接 Feature，不把跨模块跳转规则下沉到任一业务模块。 */
@Composable
fun AppNavigation() {
    AppNavHost(
        startDestination = CartRoute.Main,
        serializersModule = appNavigationSerializers,
    ) { navigator ->
        cartEntryProvider { _ ->
            navigator.navigate(LoginRoute.Main)
        }
        loginEntryProvider {
            navigator.navigateBack()
        }
    }
}
