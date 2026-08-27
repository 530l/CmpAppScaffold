package com.lyf.cmp.database

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal object DatabaseIos {
    @OptIn(ExperimentalForeignApi::class)
    fun createDatabase(): AppDatabase {
        val directory = requireNotNull(
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSApplicationSupportDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )?.path,
        ) { "无法创建 iOS Application Support 目录" }

        return Room.databaseBuilder<AppDatabase>(name = "$directory/$DATABASE_NAME")
            .buildAppDatabase()
    }
}
