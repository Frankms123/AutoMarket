package controller

import data.repository.VehicleRepository
import entity.VehicleDto
import java.io.File

class VehicleFormViewModel : BaseViewModel<VehicleDto>() {

    private val vehicleRepository = VehicleRepository()

    fun loadVehicleForEditing(vehicleId: Int) {
        executeLoadCall(
            apiCall = { vehicleRepository.getVehicleById(vehicleId) },
            errorMessage = "Error loading vehicle data."
        )
    }

    fun saveVehicle(
        vehicleId: Int?,
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
        imageFile: File?
    ) {
        if (vehicleId != null) {
            updateVehicle(vehicleId, brand, model, year, price, mileage, description, type, transmission, condition, ownerId)
        } else {
            createVehicle(brand, model, year, price, mileage, description, type, transmission, condition, ownerId, imageFile)
        }
    }

    private fun createVehicle(
        brand: String, model: String, year: Int, price: Double, mileage: Int,
        description: String, type: String, transmission: String, condition: String,
        ownerId: Int, imageFile: File?
    ) {
        if (imageFile == null) {
            _uiState.value = UiState.Error("Please select an image for the vehicle.")
            return
        }
        executeActionCall(
            apiCall = { vehicleRepository.createVehicle(brand, model, year, price, mileage, description, type, transmission, condition, ownerId, imageFile) },
            successMessage = "Vehicle created successfully!",
            errorMessage = "Error creating vehicle."
        )
    }

    private fun updateVehicle(
        vehicleId: Int, brand: String, model: String, year: Int, price: Double, mileage: Int,
        description: String, type: String, transmission: String, condition: String, ownerId: Int
    ) {
        executeActionCall(
            apiCall = { vehicleRepository.updateVehicle(vehicleId, brand, model, year, price, mileage, description, type, transmission, condition, ownerId) },
            successMessage = "Vehicle updated successfully!",
            errorMessage = "Error updating vehicle."
        )
    }
}
