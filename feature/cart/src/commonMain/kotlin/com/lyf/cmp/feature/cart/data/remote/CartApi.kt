package com.lyf.cmp.feature.cart.data.remote

import de.jensklingenberg.ktorfit.http.GET
import io.ktor.client.statement.HttpResponse

/**
 * 远端购物车接口。路径是脚手架约定，接入真实后端时在此集中调整。
 */
interface CartApi {
    @GET("v1/cart")
    suspend fun getCart(): HttpResponse
}
