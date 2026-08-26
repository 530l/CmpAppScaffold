package com.lyf.cmp.core.init

import com.lyf.cmp.core.config.AppConfig
import com.lyf.cmp.core.storage.initPlatformStorage
import com.lyf.cmp.database.AppDatabase
import com.lyf.cmp.database.createDatabase
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
