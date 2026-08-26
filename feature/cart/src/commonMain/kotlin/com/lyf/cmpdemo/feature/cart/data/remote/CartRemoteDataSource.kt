package com.lyf.cmpdemo.feature.cart.data.remote

import com.lyf.cmpdemo.core.network.NetworkResult
import com.lyf.cmpdemo.core.network.safeRequest

/** API 接口与 Repository 之间的边界，统一在这里完成网络错误转换。 */
class CartRemoteDataSource(
    private val api: CartApi,
) {
    suspend fun getCart(): NetworkResult<List<CartItemDto>> = safeRequest {
        api.getCart()
    }
}
