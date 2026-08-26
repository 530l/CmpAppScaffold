package com.lyf.cmpdemo.navigation

import androidx.compose.runtime.Composable
import com.lyf.cmpdemo.core.navigation.AppNavHost
import com.lyf.cmpdemo.feature.cart.navigation.CartRoute
import com.lyf.cmpdemo.feature.cart.navigation.cartEntryProvider
import com.lyf.cmpdemo.feature.cart.navigation.cartNavigationSerializers
import com.lyf.cmpdemo.feature.login.navigation.LoginRoute
import com.lyf.cmpdemo.feature.login.navigation.loginEntryProvider
import com.lyf.cmpdemo.feature.login.navigation.loginNavigationSerializers
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
