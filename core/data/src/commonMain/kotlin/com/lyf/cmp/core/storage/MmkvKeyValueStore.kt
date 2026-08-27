package com.lyf.cmp.core.storage

import com.tencent.mmkv.kmp.MMKV

/**
 * [KeyValueStore] 的 MMKV 实现。
 *
 * - 使用具名实例（mmapID）而非 defaultMMKV()，与库内其他默认存储隔离；
 * - 必须在平台 MMKV 初始化（StorageAndroid/StorageIos.initPlatformStorage）完成后才能构造
 *   （由启动链路保证：Application.onCreate / MainViewController 构造时即初始化，早于任何 Koin 解析）；
 * - MMKV 自身线程安全，实例可在多协程间共享（Koin single）；
 * - 需要加密时走 MMKVConfig 的 cryptKey（密钥须来自平台安全存储，勿硬编码），
 *   届时在 CoreModule 中集中调整，业务仍只依赖接口。
 */
class MmkvKeyValueStore(
    mmapID: String = DEFAULT_MMAP_ID,
) : KeyValueStore {
    private val mmkv: MMKV = MMKV.mmkvWithID(mmapID)

    override fun putString(key: String, value: String): Boolean = mmkv.encodeString(key, value)

    override fun getString(key: String, default: String?): String? =
        mmkv.decodeString(key, default)

    override fun putBoolean(key: String, value: Boolean): Boolean = mmkv.encodeBool(key, value)

    override fun getBoolean(key: String, default: Boolean): Boolean =
        mmkv.decodeBool(key, default)

    override fun putInt(key: String, value: Int): Boolean = mmkv.encodeInt(key, value)

    override fun getInt(key: String, default: Int): Int = mmkv.decodeInt(key, default)

    override fun putLong(key: String, value: Long): Boolean = mmkv.encodeLong(key, value)

    override fun getLong(key: String, default: Long): Long = mmkv.decodeLong(key, default)

    override fun putFloat(key: String, value: Float): Boolean = mmkv.encodeFloat(key, value)

    override fun getFloat(key: String, default: Float): Float = mmkv.decodeFloat(key, default)

    override fun putBytes(key: String, value: ByteArray): Boolean = mmkv.encodeBytes(key, value)

    override fun getBytes(key: String): ByteArray? = mmkv.decodeBytes(key)

    override fun contains(key: String): Boolean = mmkv.containsKey(key)

    override fun remove(key: String) = mmkv.removeValueForKey(key)

    override fun remove(keys: List<String>) = mmkv.removeValuesForKeys(keys)

    override fun clearAll() = mmkv.clearAll()

    override fun sync() = mmkv.sync()

    private companion object {
        const val DEFAULT_MMAP_ID = "cmp_common"
    }
}
