package com.lyf.cmp.navigation

import androidx.compose.runtime.Composable

/** iOS 应用不响应主动退出策略。 */
@Suppress("UNUSED_PARAMETER")
@Composable
internal actual fun PlatformExitHandler(
    isTabRoot: Boolean,
    isHomeRoot: Boolean,
    onNavigateHome: () -> Unit,
) = Unit
