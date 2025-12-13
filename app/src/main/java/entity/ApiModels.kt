package entity

import com.google.gson.annotations.SerializedName

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

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class VehicleDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("marca")
    val brand: String,
    @SerializedName("modelo")
    val model: String,
    @SerializedName("anio")
    val year: Int,
    @SerializedName("precio")
    val price: Double,
    @SerializedName("kilometraje")
    val mileage: Int,
    @SerializedName("descripcion")
    val description: String,
    @SerializedName("tipo")
    val type: String,
    @SerializedName("transmision")
    val transmission: String,
    @SerializedName("estado")
    val condition: String,
    @SerializedName("imagen_url")
    val imageUrl: String?,
    @SerializedName("owner_id")
    val ownerId: Int
)
