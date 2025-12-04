package com.Frank.automarket.data.network

import com.Frank.automarket.data.network.model.LoginPayload
import com.Frank.automarket.data.network.model.RegisterPayload
import com.Frank.automarket.data.network.model.UserDto
import com.Frank.automarket.data.network.model.VehiculoDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Usuarios ---

    @POST("users/")
    suspend fun register(@Body payload: RegisterPayload): Response<UserDto>

    @POST("users/login")
    suspend fun login(@Body payload: LoginPayload): Response<UserDto>

    // --- Vehículos ---

    @GET("vehiculos/")
    suspend fun getVehiculos(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<VehiculoDto>>

    @GET("vehiculos/search/")
    suspend fun searchVehiculos(@Query("query") query: String): Response<List<VehiculoDto>>

    @GET("vehiculos/{id}")
    suspend fun getVehiculoById(@Path("id") vehiculoId: Int): Response<VehiculoDto>

    @Multipart
    @POST("vehiculos/")
    suspend fun createVehiculo(
        @Part("marca") marca: RequestBody,
        @Part("modelo") modelo: RequestBody,
        @Part("anio") anio: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part("kilometraje") kilometraje: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part("transmision") transmision: RequestBody,
        @Part("estado") estado: RequestBody,
        @Part("owner_id") ownerId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<VehiculoDto>

    @PUT("vehiculos/{id}")
    suspend fun updateVehiculo(
        @Path("id") vehiculoId: Int,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<VehiculoDto>

    @DELETE("vehiculos/{id}")
    suspend fun deleteVehiculo(
        @Path("id") vehiculoId: Int
    ): Response<Unit>
}