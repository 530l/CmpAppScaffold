package com.lyf.cmpdemo.core.init

import org.koin.core.context.startKoin

// 应用级初始化入口（两端统一）：
// Android：Application.onCreate 调 initSharedApp(this)
// iOS：MainViewController 首次创建时调 initSharedApp()（主线程）
// common 只负责 Koin；MMKV 等平台初始化见各平台 actual
private var initialized = false

fun initSharedApp(context: Any? = null) {
    if (initialized) return
    initialized = true
    startKoin {
        modules() // feature module 在各自 CartModule 等处定义后注册于此
    }
    initPlatform(context)
}

// 平台初始化：Android 传 Application，iOS 传 null
internal expect fun initPlatform(context: Any?)
