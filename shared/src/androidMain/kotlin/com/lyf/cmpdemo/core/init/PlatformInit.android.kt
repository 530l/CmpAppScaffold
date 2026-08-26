package com.lyf.cmpdemo.core.init

import android.content.Context
import com.lyf.cmpdemo.core.config.AppConfig
import com.lyf.cmpdemo.database.AppDatabase
import com.lyf.cmpdemo.database.createDatabase
import org.koin.dsl.module

fun initSharedApp(
    context: Context,
    config: AppConfig = AppConfig(),
) {
    val appContext = context.applicationContext
    initializeSharedApp(
        config = config,
        platformModule = module {
            single { createDatabase(appContext) }
            single { get<AppDatabase>().cartDao() }
        },
    )
}
