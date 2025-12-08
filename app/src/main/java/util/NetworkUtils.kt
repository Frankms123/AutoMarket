package util

import retrofit2.Response

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error("Response body is null")
        } else {
            Resource.Error(response.errorBody()?.string() ?: "Unknown API error")
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Network exception")
    }
}
