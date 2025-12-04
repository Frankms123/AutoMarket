package com.Frank.automarket.data.repository

import com.Frank.automarket.data.network.ApiClient
import com.Frank.automarket.data.network.model.VehiculoDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class VehiculoRepository {

    private val apiService = ApiClient.instance

    suspend fun getVehiculos(skip: Int = 0, limit: Int = 20): Response<List<VehiculoDto>> {
        return apiService.getVehiculos(skip, limit)
    }

    suspend fun searchVehiculos(query: String): Response<List<VehiculoDto>> {
        return apiService.searchVehiculos(query)
    }

    suspend fun getVehiculoById(id: Int): Response<VehiculoDto> {
        return apiService.getVehiculoById(id)
    }

    suspend fun createVehiculo(
        marca: String,
        modelo: String,
        anio: Int,
        precio: Double,
        kilometraje: Int,
        descripcion: String,
        tipo: String,
        transmision: String,
        estado: String,
        ownerId: Int,
        imageFile: File
    ): Response<VehiculoDto> {
        val marcaBody = marca.toRequestBody("text/plain".toMediaTypeOrNull())
        val modeloBody = modelo.toRequestBody("text/plain".toMediaTypeOrNull())
        val anioBody = anio.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val precioBody = precio.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val kilometrajeBody = kilometraje.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descripcionBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val tipoBody = tipo.toRequestBody("text/plain".toMediaTypeOrNull())
        val transmisionBody = transmision.toRequestBody("text/plain".toMediaTypeOrNull())
        val estadoBody = estado.toRequestBody("text/plain".toMediaTypeOrNull())
        val ownerIdBody = ownerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

        return apiService.createVehiculo(
            marca = marcaBody,
            modelo = modeloBody,
            anio = anioBody,
            precio = precioBody,
            kilometraje = kilometrajeBody,
            descripcion = descripcionBody,
            tipo = tipoBody,
            transmision = transmisionBody,
            estado = estadoBody,
            ownerId = ownerIdBody,
            file = imagePart
        )
    }

    suspend fun updateVehiculo(
        vehiculoId: Int,
        marca: String,
        modelo: String,
        anio: Int,
        precio: Double,
        kilometraje: Int,
        descripcion: String,
        tipo: String,
        transmision: String,
        estado: String
    ): Response<VehiculoDto> {
        val payload = mapOf(
            "marca" to marca,
            "modelo" to modelo,
            "anio" to anio,
            "precio" to precio,
            "kilometraje" to kilometraje,
            "descripcion" to descripcion,
            "tipo" to tipo,
            "transmision" to transmision,
            "estado" to estado
        )
        return apiService.updateVehiculo(vehiculoId, payload)
    }

    suspend fun deleteVehiculo(vehiculoId: Int): Response<Unit> {
        return apiService.deleteVehiculo(vehiculoId)
    }
}