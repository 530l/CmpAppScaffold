package com.lyf.cmpdemo.core.init

import android.content.Context
import com.lyf.cmpdemo.core.config.AppConfig
import com.lyf.cmpdemo.core.storage.initPlatformStorage
import com.lyf.cmpdemo.database.AppDatabase
import com.lyf.cmpdemo.database.createDatabase
import org.koin.dsl.module

fun initSharedApp(
    context: Context,
    config: AppConfig = AppConfig(),
) {
    val appContext = context.applicationContext
    // MMKV 必须先于一切 KV 读写初始化，因此放在共享初始化最前面。
    initPlatformStorage(appContext)
    initializeSharedApp(
        config = config,
        platformModule = module {
            single { createDatabase(appContext) }
            single { get<AppDatabase>().cartDao() }
        },
    )
}
