package com.lyf.cmpdemo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lyf.cmpdemo.feature.cart.presentation.CartScreen

// 应用入口：Android 端 MainActivity 与 iOS 端 MainViewController 都调用这里
@Composable
@Preview
fun App() {
    CartScreen()
}
