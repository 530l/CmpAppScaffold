package com.lyf.cmp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 占位 Entity：Room 不允许 @Database 注册空实体列表。
 * 演示数据源已全部改为远端（购物车 = wanandroid 文章分页），暂无本地表；
 * 接入首个业务 Entity 时删除本表（新版本号 + DROP 迁移 + 新 schemas JSON）。
 */
@Entity(tableName = "schema_placeholder")
data class SchemaPlaceholderEntity(
    @PrimaryKey val id: Long = 0L,
)
