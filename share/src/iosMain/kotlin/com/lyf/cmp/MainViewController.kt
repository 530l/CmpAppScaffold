package com.lyf.cmp

import androidx.compose.ui.window.ComposeUIViewController
import com.lyf.cmp.core.init.PlatformIos
import platform.UIKit.UIViewController

// iOS 平台适配入口（Android 对应 MainActivity），Swift 壳唯一调用的共享层函数；
// 初始化收在此处而非 Swift 壳，保证主线程、先于一切 UI 代码（Android 对应 CmpApplication.onCreate）。
fun MainViewController(): UIViewController {
    // 初始化属于宿主生命周期，不在 Composable 中执行副作用。
    PlatformIos.initSharedApp()
    return ComposeUIViewController {
        App()
    }
}
