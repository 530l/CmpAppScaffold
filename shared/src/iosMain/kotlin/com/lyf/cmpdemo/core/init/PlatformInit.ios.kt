package com.lyf.cmpdemo.core.init

import com.tencent.mmkv.kmp.MMKV
import com.tencent.mmkv.kmp.initialize

actual fun initPlatform(context: Any?) {
    // MMKV：iOS 端无参初始化（要求主线程），默认根目录 Documents/mmkv
    MMKV.initialize()
}
