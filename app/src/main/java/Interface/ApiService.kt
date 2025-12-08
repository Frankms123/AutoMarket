package `interface`

import entity.LoginPayload
import entity.RegisterPayload
import entity.UserDto
import entity.VehicleDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("users/")
    suspend fun register(@Body payload: RegisterPayload): Response<UserDto>

    @POST("users/login")
    suspend fun login(@Body payload: LoginPayload): Response<UserDto>


    @GET("vehiculos/")
    suspend fun getVehicles(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<VehicleDto>>

    @GET("vehiculos/search/")
    suspend fun searchVehicles(@Query("query") query: String): Response<List<VehicleDto>>

    @GET("vehiculos/{id}")
    suspend fun getVehicleById(@Path("id") vehicleId: Int): Response<VehicleDto>

    @Multipart
    @POST("vehiculos/")
    suspend fun createVehicle(
        @Part("marca") brand: RequestBody,
        @Part("modelo") model: RequestBody,
        @Part("anio") year: RequestBody,
        @Part("precio") price: RequestBody,
        @Part("kilometraje") mileage: RequestBody,
        @Part("descripcion") description: RequestBody,
        @Part("tipo") type: RequestBody,
        @Part("transmision") transmission: RequestBody,
        @Part("estado") condition: RequestBody,
        @Part("owner_id") ownerId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<VehicleDto>

    @PUT("vehiculos/{id}")
    suspend fun updateVehicle(
        @Path("id") vehicleId: Int,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<VehicleDto>

    @DELETE("vehiculos/{id}")
    suspend fun deleteVehicle(
        @Path("id") vehicleId: Int
    ): Response<Unit>
}