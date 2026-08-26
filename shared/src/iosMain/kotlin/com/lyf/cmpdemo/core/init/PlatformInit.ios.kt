package com.lyf.cmpdemo.core.init

import com.lyf.cmpdemo.core.config.AppConfig
import com.lyf.cmpdemo.core.storage.initPlatformStorage
import com.lyf.cmpdemo.database.AppDatabase
import com.lyf.cmpdemo.database.createDatabase
import org.koin.dsl.module

fun initSharedApp(config: AppConfig = AppConfig()) {
    // MMKV 要求主线程初始化：本函数由 MainViewController() 构造调用，天然在主线程。
    initPlatformStorage()
    initializeSharedApp(
        config = config,
        platformModule = module {
            single { createDatabase() }
            single { get<AppDatabase>().cartDao() }
        },
    )
}
