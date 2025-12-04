package com.Frank.automarket.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Frank.automarket.data.network.model.VehiculoDto
import com.Frank.automarket.data.repository.VehiculoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class FormularioUiState {
    object Idle : FormularioUiState()
    object Loading : FormularioUiState()
    data class Success(val message: String) : FormularioUiState()
    data class Error(val message: String) : FormularioUiState()
    data class VehiculoLoaded(val vehiculo: VehiculoDto) : FormularioUiState()
}

class FormularioVehiculoViewModel : ViewModel() {

    private val vehiculoRepository = VehiculoRepository()

    private val _uiState = MutableStateFlow<FormularioUiState>(FormularioUiState.Idle)
    val uiState: StateFlow<FormularioUiState> = _uiState

    fun cargarVehiculoParaEdicion(vehiculoId: Int) {
        viewModelScope.launch {
            _uiState.value = FormularioUiState.Loading
            try {
                val response = vehiculoRepository.getVehiculoById(vehiculoId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = FormularioUiState.VehiculoLoaded(response.body()!!)
                } else {
                    _uiState.value = FormularioUiState.Error("Error al cargar los datos del vehículo.")
                }
            } catch (e: Exception) {
                _uiState.value = FormularioUiState.Error(e.message ?: "Excepción de red")
            }
        }
    }

    fun guardarVehiculo(
        vehiculoId: Int?,
        marca: String,
        modelo: String,
        anio: Int,
        precio: Double,
        kilometraje: Int,
        descripcion: String,
        tipo: String,
        transmision: String,
        estado: String,
        ownerId: Int, // NUEVO
        imageFile: File?
    ) {
        if (vehiculoId != null) {
            actualizarVehiculo(vehiculoId, marca, modelo, anio, precio, kilometraje, descripcion, tipo, transmision, estado)
        } else {
            crearVehiculo(marca, modelo, anio, precio, kilometraje, descripcion, tipo, transmision, estado, ownerId, imageFile)
        }
    }

    private fun crearVehiculo(
        marca: String, modelo: String, anio: Int, precio: Double, kilometraje: Int, 
        descripcion: String, tipo: String, transmision: String, estado: String, 
        ownerId: Int, imageFile: File? // NUEVO
    ) {
        if (imageFile == null) {
            _uiState.value = FormularioUiState.Error("Por favor, selecciona una imagen para el vehículo.")
            return
        }
        viewModelScope.launch {
            _uiState.value = FormularioUiState.Loading
            try {
                val response = vehiculoRepository.createVehiculo(marca, modelo, anio, precio, kilometraje, descripcion, tipo, transmision, estado, ownerId, imageFile)
                if (response.isSuccessful) {
                    _uiState.value = FormularioUiState.Success("¡Vehículo creado con éxito!")
                } else {
                    _uiState.value = FormularioUiState.Error(response.errorBody()?.string() ?: "Error al crear.")
                }
            } catch (e: Exception) {
                _uiState.value = FormularioUiState.Error(e.message ?: "Excepción de red")
            }
        }
    }

    private fun actualizarVehiculo(
        vehiculoId: Int, marca: String, modelo: String, anio: Int, precio: Double, kilometraje: Int, 
        descripcion: String, tipo: String, transmision: String, estado: String
    ) {
        viewModelScope.launch {
            _uiState.value = FormularioUiState.Loading
            try {
                val response = vehiculoRepository.updateVehiculo(vehiculoId, marca, modelo, anio, precio, kilometraje, descripcion, tipo, transmision, estado)
                if (response.isSuccessful) {
                    _uiState.value = FormularioUiState.Success("¡Vehículo actualizado con éxito!")
                } else {
                    _uiState.value = FormularioUiState.Error(response.errorBody()?.string() ?: "Error al actualizar.")
                }
            } catch (e: Exception) {
                _uiState.value = FormularioUiState.Error(e.message ?: "Excepción de red")
            }
        }
    }
}