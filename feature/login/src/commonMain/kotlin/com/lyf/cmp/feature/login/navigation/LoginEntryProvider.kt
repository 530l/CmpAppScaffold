package com.lyf.cmp.feature.login.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lyf.cmp.feature.login.presentation.LoginScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** 登录模块只暴露路由和 EntryProvider，不向其他 Feature 暴露页面拼装细节。 */
@Serializable
sealed interface LoginRoute : NavKey {
    @Serializable
    data object Main : LoginRoute
}

val loginNavigationSerializers = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(LoginRoute.Main::class, LoginRoute.Main.serializer())
    }
}

fun EntryProviderScope<NavKey>.loginEntryProvider(onBack: () -> Unit) {
    entry<LoginRoute.Main> {
        LoginScreen(onBack = onBack)
    }
}
