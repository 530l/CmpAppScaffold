package com.lyf.cmpappscaffold

import androidx.compose.runtime.Composable
import com.lyf.cmpappscaffold.core.design.AppTheme
import com.lyf.cmpappscaffold.core.image.ProvideAppImageLoader
import com.lyf.cmpappscaffold.navigation.AppNavigation

@Composable
fun App() {
    AppTheme {
        ProvideAppImageLoader {
            AppNavigation()
        }
    }
}
