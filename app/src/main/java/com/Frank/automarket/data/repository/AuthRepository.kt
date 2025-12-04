package com.Frank.automarket.data.repository

import com.Frank.automarket.data.network.ApiClient
import com.Frank.automarket.data.network.model.LoginPayload
import com.Frank.automarket.data.network.model.RegisterPayload
import com.Frank.automarket.data.network.model.UserDto
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