package com.lyf.cmpdemo.core.init

import com.lyf.cmpdemo.core.config.AppConfig
import com.lyf.cmpdemo.database.AppDatabase
import com.lyf.cmpdemo.database.createDatabase
import org.koin.dsl.module

fun initSharedApp(config: AppConfig = AppConfig()) {
    initializeSharedApp(
        config = config,
        platformModule = module {
            single { createDatabase() }
            single { get<AppDatabase>().cartDao() }
        },
    )
}
