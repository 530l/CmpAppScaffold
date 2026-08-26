package com.lyf.cmpappscaffold

import android.app.Application
import com.lyf.cmpappscaffold.core.init.initSharedApp

class CmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 在首个界面创建前完成依赖注入和数据库初始化。
        initSharedApp(this)
    }
}
