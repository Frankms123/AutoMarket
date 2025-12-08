package data.repository

import data.network.ApiClient
import entity.VehicleDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class VehicleRepository {

    private val apiService = ApiClient.instance

    suspend fun getVehicles(skip: Int = 0, limit: Int = 20): Response<List<VehicleDto>> {
        return apiService.getVehicles(skip, limit)
    }

    suspend fun searchVehicles(query: String): Response<List<VehicleDto>> {
        return apiService.searchVehicles(query)
    }

    suspend fun getVehicleById(id: Int): Response<VehicleDto> {
        return apiService.getVehicleById(id)
    }

    suspend fun createVehicle(
        brand: String,
        model: String,
        year: Int,
        price: Double,
        mileage: Int,
        description: String,
        type: String,
        transmission: String,
        condition: String,
        ownerId: Int,
        imageFile: File
    ): Response<VehicleDto> {
        val brandBody = brand.toRequestBody("text/plain".toMediaTypeOrNull())
        val modelBody = model.toRequestBody("text/plain".toMediaTypeOrNull())
        val yearBody = year.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val mileageBody = mileage.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val transmissionBody = transmission.toRequestBody("text/plain".toMediaTypeOrNull())
        val conditionBody = condition.toRequestBody("text/plain".toMediaTypeOrNull())
        val ownerIdBody = ownerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

        return apiService.createVehicle(
            brand = brandBody,
            model = modelBody,
            year = yearBody,
            price = priceBody,
            mileage = mileageBody,
            description = descriptionBody,
            type = typeBody,
            transmission = transmissionBody,
            condition = conditionBody,
            ownerId = ownerIdBody,
            file = imagePart
        )
    }

    suspend fun updateVehicle(
        vehicleId: Int,
        brand: String,
        model: String,
        year: Int,
        price: Double,
        mileage: Int,
        description: String,
        type: String,
        transmission: String,
        condition: String
    ): Response<VehicleDto> {
        val payload = mapOf(
            "brand" to brand,
            "model" to model,
            "year" to year,
            "price" to price,
            "mileage" to mileage,
            "description" to description,
            "type" to type,
            "transmission" to transmission,
            "condition" to condition
        )
        return apiService.updateVehicle(vehicleId, payload)
    }

    suspend fun deleteVehicle(vehicleId: Int): Response<Unit> {
        return apiService.deleteVehicle(vehicleId)
    }
}