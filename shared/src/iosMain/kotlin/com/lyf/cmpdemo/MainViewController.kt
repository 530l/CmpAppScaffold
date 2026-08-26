package com.lyf.cmpdemo

import androidx.compose.ui.window.ComposeUIViewController
import com.lyf.cmpdemo.core.init.initSharedApp

fun MainViewController() = ComposeUIViewController {
    // iOS 侧共享层初始化（Koin + MMKV，幂等）；Swift 壳零改动
    initSharedApp()
    App()
}
