package com.lyf.cmpdemo

import androidx.compose.runtime.Composable
import com.lyf.cmpdemo.core.design.AppTheme
import com.lyf.cmpdemo.core.image.ProvideAppImageLoader
import com.lyf.cmpdemo.navigation.AppNavigation

@Composable
fun App() {
    AppTheme {
        ProvideAppImageLoader {
            AppNavigation()
        }
    }
}
