package com.lyf.cmp.core.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T, val statusCode: Int) : NetworkResult<T>
    data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}

sealed interface NetworkError {
    data class Http(val statusCode: Int, val responseSnippet: String?) : NetworkError
    data class Connectivity(val cause: IOException) : NetworkError
    data class InvalidPayload(val cause: SerializationException) : NetworkError
    data class Unknown(val cause: Throwable) : NetworkError
}

/**
 * 网络请求的统一安全边界：保留协程取消语义，并把平台异常转换为可处理的错误模型。
 */
suspend inline fun <reified T> safeRequest(
    crossinline request: suspend () -> HttpResponse,
): NetworkResult<T> = try {
    val response = request()
    if (response.status.value in 200..299) {
        NetworkResult.Success(response.body<T>(), response.status.value)
    } else {
        NetworkResult.Failure(
            NetworkError.Http(
                statusCode = response.status.value,
                responseSnippet = response.bodyAsText().take(2_048).ifBlank { null },
            ),
        )
    }
} catch (error: CancellationException) {
    throw error
} catch (error: SerializationException) {
    NetworkResult.Failure(NetworkError.InvalidPayload(error))
} catch (error: IOException) {
    NetworkResult.Failure(NetworkError.Connectivity(error))
} catch (error: Throwable) {
    NetworkResult.Failure(NetworkError.Unknown(error))
}
