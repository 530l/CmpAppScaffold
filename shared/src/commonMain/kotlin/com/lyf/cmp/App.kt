package com.lyf.cmp

import androidx.compose.runtime.Composable
import com.lyf.cmp.core.design.AppTheme
import com.lyf.cmp.core.image.ProvideAppImageLoader
import com.lyf.cmp.navigation.AppNavigation

@Composable
fun App() {
    AppTheme {
        ProvideAppImageLoader {
            AppNavigation()
        }
    }
}
