package com.lyf.cmp.core.network

import com.lyf.cmp.core.config.AppConfig
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient

/** Ktorfit 只负责声明式接口生成，底层仍复用应用级 Ktor 客户端。 */
fun createKtorfit(
    config: AppConfig,
    httpClient: HttpClient,
): Ktorfit = Ktorfit.Builder()
    .baseUrl(config.apiBaseUrl)
    .httpClient(httpClient)
    .build()
