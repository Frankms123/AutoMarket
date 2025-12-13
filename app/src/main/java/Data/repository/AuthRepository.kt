package data.repository

import data.network.ApiClient
import entity.LoginPayload
import entity.RegisterPayload
import entity.UserDto
import retrofit2.Response

class AuthRepository {

    private val apiService = ApiClient.instance

    suspend fun register(email: String, password: String): Response<UserDto> {
        val payload = RegisterPayload(email = email, password = password)
        return apiService.register(payload)
    }

    suspend fun login(email: String, password: String): Response<UserDto> {
        val payload = LoginPayload(email = email, password = password)
        return apiService.login(payload)
    }
}