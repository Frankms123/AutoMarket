package Data


import android.content.Context
import entity.Vehiculo
import kotlinx.coroutines.flow.Flow

class MemoryDataManager(context: Context) : DataManager {

    private val database = AppDatabase.getDatabase(context)
    private val vehiculoDao = database.vehiculoDao()

    override fun getAllVehiculos(): Flow<List<Vehiculo>> {
        return vehiculoDao.getAllVehiculos()
    }

    override fun searchVehiculos(query: String): Flow<List<Vehiculo>> {
        return vehiculoDao.searchVehiculos(query)
    }

    override suspend fun getVehiculoById(id: Long): Vehiculo? {
        return vehiculoDao.getVehiculoById(id)
    }

    override suspend fun getVehiculosCount(): Int {
        return vehiculoDao.getVehiculosCount()
    }

    override suspend fun insertVehiculo(vehiculo: Vehiculo): Long {
        val validation = validateVehiculo(vehiculo)
        if (!validation.isValid) {
            throw IllegalArgumentException(validation.errors.joinToString(", "))
        }
        return vehiculoDao.insertVehiculo(vehiculo)
    }

    override suspend fun updateVehiculo(vehiculo: Vehiculo) {
        val validation = validateVehiculo(vehiculo)
        if (!validation.isValid) {
            throw IllegalArgumentException(validation.errors.joinToString(", "))
        }
        vehiculoDao.updateVehiculo(vehiculo)
    }

    override suspend fun deleteVehiculo(vehiculo: Vehiculo) {
        vehiculoDao.deleteVehiculo(vehiculo)
    }

    override suspend fun deleteAllVehiculos() {
        vehiculoDao.deleteAllVehiculos()
    }

    override fun validateVehiculo(vehiculo: Vehiculo): ValidationResult {
        val errors = mutableListOf<String>()

        if (vehiculo.marca.isBlank()) {
            errors.add("La marca es requerida")
        }

        if (vehiculo.modelo.isBlank()) {
            errors.add("El modelo es requerido")
        }

        if (vehiculo.anio < 1900 || vehiculo.anio > 2030) {
            errors.add("El año debe estar entre 1900 y 2030")
        }

        if (vehiculo.precio <= 0) {
            errors.add("El precio debe ser mayor a 0")
        }

        if (vehiculo.kilometraje < 0) {
            errors.add("El kilometraje no puede ser negativo")
        }

        if (vehiculo.transmision !in listOf("Manual", "Automática")) {
            errors.add("Transmisión inválida")
        }

        if (vehiculo.estado !in listOf("Nuevo", "Usado")) {
            errors.add("Estado inválido")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}