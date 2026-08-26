package com.lyf.cmp.feature.cart.data

import com.lyf.cmp.core.model.CurrencyCode
import com.lyf.cmp.core.model.Money
import com.lyf.cmp.core.network.NetworkResult
import com.lyf.cmp.feature.cart.data.local.CartDao
import com.lyf.cmp.feature.cart.data.local.CartItemEntity
import com.lyf.cmp.feature.cart.data.remote.CartItemDto
import com.lyf.cmp.feature.cart.data.remote.CartRemoteDataSource
import com.lyf.cmp.feature.cart.domain.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface CartRepository {
    fun observeCartItems(): Flow<List<CartItem>>
    suspend fun ensureSeeded()
    suspend fun refreshFromRemote(): NetworkResult<Unit>
    suspend fun toggleSelection(itemId: Long)
    suspend fun setAllSelected(selected: Boolean)
}

class DefaultCartRepository(
    private val dao: CartDao,
    private val remoteDataSource: CartRemoteDataSource,
) : CartRepository {
    override fun observeCartItems(): Flow<List<CartItem>> =
        dao.observeAll().map { entities -> entities.map(CartItemEntity::toDomain) }

    override suspend fun ensureSeeded() {
        if (dao.count() == 0) dao.insertAll(SEED_ITEMS)
    }

    override suspend fun refreshFromRemote(): NetworkResult<Unit> =
        when (val result = remoteDataSource.getCart()) {
            is NetworkResult.Success -> {
                dao.replaceAll(result.value.map(CartItemDto::toEntity))
                NetworkResult.Success(Unit, result.statusCode)
            }

            is NetworkResult.Failure -> result
        }

    override suspend fun toggleSelection(itemId: Long) {
        dao.toggleSelection(itemId)
    }

    override suspend fun setAllSelected(selected: Boolean) {
        dao.setAllSelected(selected)
    }
}

private fun CartItemEntity.toDomain(): CartItem = CartItem(
    id = id,
    name = name,
    unitPrice = Money(
        minorUnits = priceMinor,
        currency = CurrencyCode.valueOf(currencyCode),
    ),
    count = count,
    emoji = emoji,
    imageUrl = imageUrl,
    selected = selected,
)

private fun CartItemDto.toEntity(): CartItemEntity = CartItemEntity(
    id = id,
    name = name,
    priceMinor = priceMinor,
    currencyCode = currencyCode,
    count = count,
    emoji = emoji,
    imageUrl = imageUrl,
    selected = selected,
)

private val SEED_ITEMS = listOf(
    CartItemEntity(1, "挂耳咖啡 · 10 包装", 1_250, "CNY", 2, "☕", null, false),
    CartItemEntity(2, "陶瓷马克杯 380ml", 880, "CNY", 1, "🍵", null, false),
    CartItemEntity(3, "无线蓝牙耳机 · 半入耳", 19_900, "CNY", 1, "🎧", null, false),
    CartItemEntity(4, "快充充电宝 20000mAh", 12_900, "CNY", 1, "🔋", null, false),
    CartItemEntity(5, "棉麻收纳袋 · 三件套", 3_990, "CNY", 3, "🧺", null, false),
    CartItemEntity(6, "极简木质台历 2026", 2_500, "CNY", 1, "📅", null, false),
)
