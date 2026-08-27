package com.lyf.cmp.core.network

import com.lyf.cmp.core.config.AppConfig
import com.lyf.cmp.core.log.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

private const val REQUEST_TIMEOUT_MILLIS = 30_000L
private const val CONNECT_TIMEOUT_MILLIS = 15_000L

fun createHttpClient(config: AppConfig): HttpClient = HttpClient {
    expectSuccess = false

    defaultRequest {
        url(config.apiBaseUrl)
        accept(ContentType.Application.Json)
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            },
        )
    }

    install(HttpRequestRetry) {
        maxRetries = 2
        retryIf { request, response ->
            request.method in IDEMPOTENT_METHODS && response.status.value in 500..599
        }
        retryOnExceptionIf { request, error ->
            // 仅重试传输层异常；反序列化、参数和业务异常重试不会产生不同结果。
            error is IOException && request.method in IDEMPOTENT_METHODS
        }
        exponentialDelay(maxDelayMs = 2_000)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }

    if (config.enableNetworkLogging) {
        install(Logging) {
            logger = object : KtorLogger {
                override fun log(message: String) {
                    AppLogger.debug("HttpClient") { message }
                }
            }
            // 商业项目默认不打印正文，避免 token、手机号等敏感信息进入日志。
            level = LogLevel.HEADERS
            sanitizeHeader { header ->
                header == HttpHeaders.Authorization ||
                    header == HttpHeaders.Cookie ||
                    header == HttpHeaders.SetCookie
            }
        }
    }
}

private val IDEMPOTENT_METHODS = setOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Options)
