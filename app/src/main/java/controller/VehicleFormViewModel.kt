package controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.repository.VehicleRepository
import entity.VehicleDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import util.Resource
import util.safeApiCall
import java.io.File

sealed class FormUiState {
    object Idle : FormUiState()
    object Loading : FormUiState()
    data class Success(val message: String) : FormUiState()
    data class Error(val message: String) : FormUiState()
    data class VehicleLoaded(val vehicle: VehicleDto) : FormUiState()
}

class VehicleFormViewModel : ViewModel() {

    private val vehicleRepository = VehicleRepository()

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState

    fun loadVehicleForEditing(vehicleId: Int) {
        viewModelScope.launch {
            _uiState.value = FormUiState.Loading
            when (val resource = safeApiCall { vehicleRepository.getVehicleById(vehicleId) }) {
                is Resource.Success -> {
                    _uiState.value = FormUiState.VehicleLoaded(resource.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = FormUiState.Error(resource.message ?: "Error loading vehicle data.")
                }
            }
        }
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
            updateVehicle(vehicleId, brand, model, year, price, mileage, description, type, transmission, condition)
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
            _uiState.value = FormUiState.Error("Please select an image for the vehicle.")
            return
        }
        viewModelScope.launch {
            _uiState.value = FormUiState.Loading
            val resource = safeApiCall {
                vehicleRepository.createVehicle(brand, model, year, price, mileage, description, type, transmission, condition, ownerId, imageFile)
            }
            when (resource) {
                is Resource.Success -> {
                    _uiState.value = FormUiState.Success("Vehicle created successfully!")
                }
                is Resource.Error -> {
                    _uiState.value = FormUiState.Error(resource.message ?: "Error creating vehicle.")
                }
            }
        }
    }

    private fun updateVehicle(
        vehicleId: Int, brand: String, model: String, year: Int, price: Double, mileage: Int, 
        description: String, type: String, transmission: String, condition: String
    ) {
        viewModelScope.launch {
            _uiState.value = FormUiState.Loading
            val resource = safeApiCall {
                vehicleRepository.updateVehicle(vehicleId, brand, model, year, price, mileage, description, type, transmission, condition)
            }
            when (resource) {
                is Resource.Success -> {
                    _uiState.value = FormUiState.Success("Vehicle updated successfully!")
                }
                is Resource.Error -> {
                    _uiState.value = FormUiState.Error(resource.message ?: "Error updating vehicle.")
                }
            }
        }
    }
}