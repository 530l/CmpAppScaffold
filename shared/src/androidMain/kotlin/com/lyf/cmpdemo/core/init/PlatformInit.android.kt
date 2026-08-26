package com.lyf.cmpdemo.core.init

import android.content.Context
import com.tencent.mmkv.kmp.MMKV
import com.tencent.mmkv.kmp.initialize

actual fun initPlatform(context: Any?) {
    // MMKV：Android 端必须传 Application Context（通常在 Application.onCreate）
    MMKV.initialize(context as Context)
}
