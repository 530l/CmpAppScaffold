package com.lyf.cmpdemo

import androidx.compose.ui.window.ComposeUIViewController
import com.lyf.cmpdemo.core.init.initSharedApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // 初始化属于宿主生命周期，不在 Composable 中执行副作用。
    initSharedApp()
    return ComposeUIViewController {
        App()
    }
}
