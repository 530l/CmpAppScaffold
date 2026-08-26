package com.lyf.cmpdemo.core.storage

import android.content.Context
import com.tencent.mmkv.kmp.MMKV
import com.tencent.mmkv.kmp.MMKVLogLevel
import com.tencent.mmkv.kmp.initialize

/**
 * Android 侧 MMKV 初始化：必须先于一切 KV 读写，由 shared 的启动链路
 * （Application.onCreate → initSharedApp）最先调用。
 * 根目录默认 $(FilesDir)/mmkv；日志级别压到 Warning，避免 Info 噪声进入 release。
 */
fun initPlatformStorage(context: Context) {
    MMKV.initialize(context, logLevel = MMKVLogLevel.Warning)
}
