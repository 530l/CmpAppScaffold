package com.lyf.cmpappscaffold.core.di

import com.lyf.cmpappscaffold.core.config.AppConfig
import com.lyf.cmpappscaffold.core.network.createHttpClient
import com.lyf.cmpappscaffold.core.network.createKtorfit
import com.lyf.cmpappscaffold.core.storage.KeyValueStore
import com.lyf.cmpappscaffold.core.storage.MmkvKeyValueStore
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
