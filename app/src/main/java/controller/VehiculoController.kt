package controller

import android.content.Context
import Data.AppDatabase
import Entity.ResultadoOperacion
import Entity.Vehiculo
import Entity.VehiculoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VehiculoController(context: Context) {

    private val vehiculoDao: VehiculoDao = AppDatabase.getDatabase(context).vehiculoDao()

    fun obtenerTodosLosVehiculos(): Flow<List<Vehiculo>> {
        return vehiculoDao.getAllVehiculos()
    }

    fun buscarVehiculos(query: String): Flow<List<Vehiculo>> {
        return vehiculoDao.searchVehiculos(query)
    }

    suspend fun obtenerVehiculoPorId(id: Long): ResultadoOperacion<Vehiculo?> = withContext(Dispatchers.IO) {
        try {
            vehiculoDao.getVehiculoById(id)?.let {
                ResultadoOperacion.Exito(it)
            } ?: ResultadoOperacion.Error("Vehículo no encontrado")
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al obtener el vehículo: ${e.message}", e)
        }
    }

    suspend fun crearVehiculo(vehiculo: Vehiculo): ResultadoOperacion<Long> = withContext(Dispatchers.IO) {
        try {
            val newId = vehiculoDao.insertVehiculo(vehiculo)
            ResultadoOperacion.Exito(newId)
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al crear el vehículo: ${e.message}", e)
        }
    }

    suspend fun actualizarVehiculo(vehiculo: Vehiculo): ResultadoOperacion<Unit> = withContext(Dispatchers.IO) {
        try {
            vehiculoDao.updateVehiculo(vehiculo)
            ResultadoOperacion.Exito(Unit)
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al actualizar el vehículo: ${e.message}", e)
        }
    }

    suspend fun eliminarVehiculo(vehiculo: Vehiculo): ResultadoOperacion<Unit> = withContext(Dispatchers.IO) {
        try {
            vehiculoDao.deleteVehiculo(vehiculo)
            ResultadoOperacion.Exito(Unit)
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al eliminar el vehículo: ${e.message}", e)
        }
    }
}