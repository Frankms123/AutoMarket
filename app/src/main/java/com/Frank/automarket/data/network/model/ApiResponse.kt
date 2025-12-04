package com.Frank.automarket.data.network.model

import com.google.gson.annotations.SerializedName

// --- Payloads para Peticiones ---

data class RegisterPayload(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginPayload(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// --- DTOs de Respuestas ---

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class VehiculoDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("marca")
    val marca: String,
    @SerializedName("modelo")
    val modelo: String,
    @SerializedName("anio")
    val anio: Int,
    @SerializedName("precio")
    val precio: Double,
    @SerializedName("kilometraje")
    val kilometraje: Int,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("tipo")
    val tipo: String,
    @SerializedName("transmision")
    val transmision: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("imagen_url")
    val imagenUrl: String?,
    @SerializedName("owner_id")
    val ownerId: Int
)
