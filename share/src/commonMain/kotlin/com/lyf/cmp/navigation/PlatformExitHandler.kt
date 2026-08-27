package com.lyf.cmp.navigation

import androidx.compose.runtime.Composable

/** Android 在 Tab 根页处理返回首页或二次退出；iOS 不主动处理系统返回。 */
@Composable
internal expect fun PlatformExitHandler(
    isTabRoot: Boolean,
    isHomeRoot: Boolean,
    onNavigateHome: () -> Unit,
)
