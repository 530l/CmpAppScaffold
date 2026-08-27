package com.lyf.cmp.core.init

import com.lyf.cmp.core.config.AppConfig
import com.lyf.cmp.core.storage.StorageIos
import com.lyf.cmp.database.DatabaseIos
import org.koin.dsl.module

/** iOS 平台初始化入口单例（Android 侧 PlatformAndroid 对称）；
 * 幂等与重试语义由 common 的 initializeSharedApp 保证。 */
object PlatformIos {
    fun initSharedApp(config: AppConfig = AppConfig()) {
        // MMKV 要求主线程初始化：本方法由 MainViewController() 构造调用，天然在主线程。
        StorageIos.initPlatformStorage()
        initializeSharedApp(
            config = config,
            platformModule = module {
                single { DatabaseIos.createDatabase() }
            },
        )
    }
}
