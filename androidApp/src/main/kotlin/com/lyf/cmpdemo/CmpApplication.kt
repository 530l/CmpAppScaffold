package com.lyf.cmpdemo

import android.app.Application
import com.lyf.cmpdemo.core.init.initSharedApp

class CmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 共享层初始化：Koin + MMKV（Android 端需传 Context）
        initSharedApp(this)
    }
}
