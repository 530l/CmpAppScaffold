package com.lyf.cmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lyf.cmp.core.design.AppTheme
import com.lyf.cmp.core.image.ProvideAppImageLoader
import com.lyf.cmp.navigation.AppNavigation
import io.ktor.client.HttpClient
import org.koin.compose.koinInject

// 共享 UI 根入口：iOS MainViewController()/Android MainActivity 两个平台适配入口
// 都挂载本 Composable，往下全部是平台无关代码。
@Composable
fun App() {
    // 依赖注入留在应用组合根，core:design 只接收构造图片加载器所需的显式依赖。
    val httpClient = koinInject<HttpClient>()
    AppTheme {
        ProvideAppImageLoader(httpClient = httpClient) {
            // 应用背景覆盖完整物理窗口，页面内容再按需消费系统安全区。
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AppNavigation()
            }
        }
    }
}
