package com.lyf.cmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// Android 平台适配入口（iOS 对应 share 模块 iosMain 的 MainViewController.kt）：
// 只做平台容器挂载，双端最终都汇聚到 commonMain 的 App()。
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }
}
