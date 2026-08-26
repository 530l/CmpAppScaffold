package com.lyf.cmpdemo.core.init

import com.lyf.cmpdemo.core.config.AppConfig
import com.lyf.cmpdemo.core.di.coreModule
import com.lyf.cmpdemo.feature.cart.cartModule
import kotlinx.atomicfu.atomic
import org.koin.core.context.startKoin
import org.koin.core.module.Module

private val initialized = atomic(false)

/** 两个平台共用的幂等初始化边界。初始化失败时允许下一次重试。 */
internal fun initializeSharedApp(
    config: AppConfig,
    platformModule: Module,
) {
    if (!initialized.compareAndSet(expect = false, update = true)) return

    try {
        startKoin {
            allowOverride(false)
            modules(coreModule(config), platformModule, cartModule)
        }
    } catch (error: Throwable) {
        initialized.value = false
        throw error
    }
}
