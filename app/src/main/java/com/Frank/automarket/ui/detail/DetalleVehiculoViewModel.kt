package com.Frank.automarket.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Frank.automarket.data.network.model.VehiculoDto
import com.Frank.automarket.data.repository.VehiculoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los diferentes estados de la UI de la pantalla de Detalle.
 */
sealed class DetalleUiState {
    object Loading : DetalleUiState()
    data class Success(val vehiculo: VehiculoDto) : DetalleUiState()
    data class Error(val message: String) : DetalleUiState()
    object Deleted : DetalleUiState()
}

/**
 * ViewModel para la DetalleVehiculoActivity.
 */
class DetalleVehiculoViewModel : ViewModel() {

    private val vehiculoRepository = VehiculoRepository()

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Loading)
    val uiState: StateFlow<DetalleUiState> = _uiState

    fun cargarDetallesVehiculo(vehiculoId: Int) {
        viewModelScope.launch {
            _uiState.value = DetalleUiState.Loading
            try {
                val response = vehiculoRepository.getVehiculoById(vehiculoId)

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = DetalleUiState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido al cargar el vehículo"
                    _uiState.value = DetalleUiState.Error(errorBody)
                }
            } catch (e: Exception) {
                _uiState.value = DetalleUiState.Error(e.message ?: "Ocurrió una excepción de red")
            }
        }
    }

    /**
     * Elimina un vehículo a través de la API.
     */
    fun eliminarVehiculo(vehiculoId: Int) {
        viewModelScope.launch {
            _uiState.value = DetalleUiState.Loading // Muestra un estado de carga mientras se elimina
            try {
                val response = vehiculoRepository.deleteVehiculo(vehiculoId)

                if (response.isSuccessful) {
                    _uiState.value = DetalleUiState.Deleted
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido al eliminar el vehículo"
                    _uiState.value = DetalleUiState.Error(errorBody)
                }
            } catch (e: Exception) {
                _uiState.value = DetalleUiState.Error(e.message ?: "Ocurrió una excepción de red")
            }
        }
    }
}