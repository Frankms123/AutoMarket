package util

import retrofit2.Response

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

suspend fun <T> safeApiCall(allowEmptyBody: Boolean = false, apiCall: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Resource.Success(body)
            } else if (allowEmptyBody && response.code() == 204) {
                @Suppress("UNCHECKED_CAST")
                Resource.Success(Unit as T)
            } else {
                Resource.Error("Response body is null")
            }
        } else {
            Resource.Error(response.errorBody()?.string() ?: "Unknown API error")
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Network exception")
    }
}
