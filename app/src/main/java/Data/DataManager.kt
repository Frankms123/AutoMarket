package Data

import Entity.Vehiculo
import kotlinx.coroutines.flow.Flow

interface DataManager {
    fun getAllVehiculos(): Flow<List<Vehiculo>>
    fun searchVehiculos(query: String): Flow<List<Vehiculo>>
    suspend fun getVehiculoById(id: Long): Vehiculo?
    suspend fun getVehiculosCount(): Int
    
    suspend fun insertVehiculo(vehiculo: Vehiculo): Long
    suspend fun updateVehiculo(vehiculo: Vehiculo)
    suspend fun deleteVehiculo(vehiculo: Vehiculo)
    suspend fun deleteAllVehiculos()
    
    fun validateVehiculo(vehiculo: Vehiculo): ValidationResult
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)