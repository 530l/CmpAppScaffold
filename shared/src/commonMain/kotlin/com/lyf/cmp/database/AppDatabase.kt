package com.lyf.cmp.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val DATABASE_NAME = "cmp.db"

/**
 * 演示数据源已切换为远端分页（wanandroid 文章列表），购物车本地表随 v2 退役；
 * Room 不允许空实体列表，暂以 [SchemaPlaceholderEntity] 占位保证管线可编译，
 * 接入首个业务 Entity 时按 README「新增业务功能约定」注册并删除占位表。
 */
@Database(
    entities = [SchemaPlaceholderEntity::class],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase()

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** v2：退役 cart_items 表，换入 Room 占位表（显式迁移，不用 destructive fallback）。 */
internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        // KMP 侧 SQLiteConnection 没有 execSQL，用 prepare + step 执行。
        connection.prepare("DROP TABLE IF EXISTS cart_items").use { it.step() }
        connection.prepare(
            "CREATE TABLE IF NOT EXISTS `schema_placeholder` " +
                "(`id` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        ).use { it.step() }
    }
}

internal fun RoomDatabase.Builder<AppDatabase>.buildAppDatabase(): AppDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2)
        .build()
