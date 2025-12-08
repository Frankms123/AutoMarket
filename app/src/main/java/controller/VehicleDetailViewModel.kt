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

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val vehicle: VehicleDto) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
    object Deleted : DetailUiState()
    data class ShowUndoDelete(val vehicle: VehicleDto) : DetailUiState()
}

class VehicleDetailViewModel : ViewModel() {

    private val vehicleRepository = VehicleRepository()

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    private var vehicleToDelete: VehicleDto? = null

    fun loadVehicleDetails(vehicleId: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            when (val resource = safeApiCall { vehicleRepository.getVehicleById(vehicleId) }) {
                is Resource.Success -> {
                    _uiState.value = DetailUiState.Success(resource.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = DetailUiState.Error(resource.message ?: "Unknown error loading vehicle")
                }
            }
        }
    }

    fun deleteVehicle(vehicle: VehicleDto) {
        vehicleToDelete = vehicle
        _uiState.value = DetailUiState.ShowUndoDelete(vehicle)
    }

    fun confirmDeletion() {
        vehicleToDelete?.let {
            viewModelScope.launch {
                _uiState.value = DetailUiState.Loading
                when (val resource = safeApiCall { vehicleRepository.deleteVehicle(it.id) }) {
                    is Resource.Success -> {
                        _uiState.value = DetailUiState.Deleted
                    }
                    is Resource.Error -> {
                        _uiState.value = DetailUiState.Error(resource.message ?: "Error deleting vehicle")
                    }
                }
            }
        }
    }

    fun undoDeletion() {
        vehicleToDelete = null
        val currentState = _uiState.value
        if (currentState is DetailUiState.ShowUndoDelete) {
            _uiState.value = DetailUiState.Success(currentState.vehicle)
        }
    }
}