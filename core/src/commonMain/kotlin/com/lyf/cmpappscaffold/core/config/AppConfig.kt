package com.lyf.cmpappscaffold.core.config

enum class AppEnvironment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION,
}

/**
 * 应用运行配置。真实项目应由各平台的构建配置注入，不要把密钥放进共享源码。
 */
data class AppConfig(
    val environment: AppEnvironment = AppEnvironment.DEVELOPMENT,
    val apiBaseUrl: String = "https://api.example.com/",
    val enableNetworkLogging: Boolean = false,
) {
    init {
        require(apiBaseUrl.startsWith("https://")) { "apiBaseUrl 必须使用 HTTPS" }
        require(apiBaseUrl.endsWith('/')) { "apiBaseUrl 必须以 / 结尾" }
    }
}
