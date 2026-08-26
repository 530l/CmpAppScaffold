package com.lyf.cmp.core.storage

import com.tencent.mmkv.kmp.MMKV
import com.tencent.mmkv.kmp.MMKVLogLevel
import com.tencent.mmkv.kmp.initialize

/**
 * iOS 侧 MMKV 初始化：库要求必须在主线程、且先于任何读写调用——
 * MainViewController() 在主线程构造，由 shared 的 initSharedApp 调到这里即满足。
 * 根目录默认 {NSDocumentDirectory}/mmkv。
 */
fun initPlatformStorage() {
    MMKV.initialize(logLevel = MMKVLogLevel.Warning)
}
