package com.lyf.cmpdemo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lyf.cmpdemo.cart.CartScreen

// 应用入口：Android 端 MainActivity 与 iOS 端 MainViewController 都调用这里。
// 模板的 Greeting 示例已下线，改为渲染购物车（Greeting.kt 文件保留不动）
@Composable
@Preview
fun App() {
    CartScreen()
}