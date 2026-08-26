package com.lyf.cmpdemo.core.di

import com.lyf.cmpdemo.core.config.AppConfig
import com.lyf.cmpdemo.core.network.createHttpClient
import com.lyf.cmpdemo.core.network.createKtorfit
import com.lyf.cmpdemo.core.storage.KeyValueStore
import com.lyf.cmpdemo.core.storage.MmkvKeyValueStore
import io.ktor.client.HttpClient
import org.koin.dsl.module

fun coreModule(config: AppConfig) = module {
    single { config }
    single { createHttpClient(get()) }
    single { createKtorfit(config = get(), httpClient = get<HttpClient>()) }

    // single 是惰性的：首次注入才构造 MmkvKeyValueStore，但 MMKV.initialize
    // 早在启动链路（initPlatformStorage）完成，这里的构造时序天然安全。
    single<KeyValueStore> { MmkvKeyValueStore() }
}
