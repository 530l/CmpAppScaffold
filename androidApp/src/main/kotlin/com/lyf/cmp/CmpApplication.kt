package com.lyf.cmp

import android.app.Application
import com.lyf.cmp.core.init.PlatformAndroid

class CmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 在首个界面创建前完成依赖注入和数据库初始化；iOS 无等价 Application 层
        // Kotlin 钩子，对应初始化收在 iosMain 的 MainViewController() 首行。
        PlatformAndroid.initSharedApp(this)
    }
}
