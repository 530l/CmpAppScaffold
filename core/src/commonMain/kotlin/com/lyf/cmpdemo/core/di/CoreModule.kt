package com.lyf.cmpdemo.core.di

import com.lyf.cmpdemo.core.config.AppConfig
import com.lyf.cmpdemo.core.network.createHttpClient
import com.lyf.cmpdemo.core.network.createKtorfit
import io.ktor.client.HttpClient
import org.koin.dsl.module

fun coreModule(config: AppConfig) = module {
    single { config }
    single { createHttpClient(get()) }
    single { createKtorfit(config = get(), httpClient = get<HttpClient>()) }
}
