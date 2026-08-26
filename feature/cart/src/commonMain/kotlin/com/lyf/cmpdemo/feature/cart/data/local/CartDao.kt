package com.lyf.cmpdemo.feature.cart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY id")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT COUNT(*) FROM cart_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CartItemEntity>)

    @Query("UPDATE cart_items SET selected = NOT selected WHERE id = :itemId")
    suspend fun toggleSelection(itemId: Long)

    @Query("UPDATE cart_items SET selected = :selected")
    suspend fun setAllSelected(selected: Boolean)
}
