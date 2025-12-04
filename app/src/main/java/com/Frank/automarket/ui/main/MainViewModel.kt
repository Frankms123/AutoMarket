package com.Frank.automarket.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Frank.automarket.data.network.model.VehiculoDto
import com.Frank.automarket.data.repository.VehiculoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val vehiculos: List<VehiculoDto>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel : ViewModel() {

    private val vehiculoRepository = VehiculoRepository()

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        cargarVehiculos()
    }

    fun cargarVehiculos() {
        fetchVehiculos()
    }

    fun buscarVehiculos(query: String) {
        if (query.isBlank()) {
            fetchVehiculos() // Si la búsqueda está vacía, cargar todos
            return
        }

        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                val response = vehiculoRepository.searchVehiculos(query)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = MainUiState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error en la búsqueda"
                    _uiState.value = MainUiState.Error(errorBody)
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Ocurrió una excepción de red")
            }
        }
    }

    private fun fetchVehiculos() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                val response = vehiculoRepository.getVehiculos()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = MainUiState.Success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido al cargar los vehículos"
                    _uiState.value = MainUiState.Error(errorBody)
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Ocurrió una excepción de red")
            }
        }
    }
}