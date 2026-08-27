package com.lyf.cmp.core.init

import android.content.Context
import com.lyf.cmp.core.config.AppConfig
import com.lyf.cmp.core.storage.StorageAndroid
import com.lyf.cmp.database.DatabaseAndroid
import org.koin.dsl.module

/** Android 平台初始化入口单例（iOS 侧 PlatformIos 对称）；幂等与重试语义由 common 的 initializeSharedApp 保证。 */
object PlatformAndroid {
    fun initSharedApp(
        context: Context,
        config: AppConfig = AppConfig(),
    ) {
        val appContext = context.applicationContext
        // MMKV 必须先于一切 KV 读写初始化，因此放在共享初始化最前面。
        StorageAndroid.initPlatformStorage(appContext)
        initializeSharedApp(
            config = config,
            platformModule = module {
                single { DatabaseAndroid.createDatabase(appContext) }
            },
        )
    }
}
