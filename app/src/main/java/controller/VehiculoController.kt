import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import Data.MemoryDataManager
import entity.Vehiculo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VehiculoController(context: Context) {

    private val dataManager = MemoryDataManager(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    fun getAllVehiculos(): LiveData<List<Vehiculo>> {
        return dataManager.getAllVehiculos().asLiveData()
    }

    fun searchVehiculos(query: String): LiveData<List<Vehiculo>> {
        return dataManager.searchVehiculos(query).asLiveData()
    }

    fun getVehiculoById(id: Long, callback: (Vehiculo?) -> Unit) {
        scope.launch {
            val vehiculo = withContext(Dispatchers.IO) {
                dataManager.getVehiculoById(id)
            }
            callback(vehiculo)
        }
    }

    fun insertVehiculo(vehiculo: Vehiculo, callback: (Result<Long>) -> Unit) {
        scope.launch {
            try {
                val id = withContext(Dispatchers.IO) {
                    dataManager.insertVehiculo(vehiculo)
                }
                callback(Result.success(id))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    fun updateVehiculo(vehiculo: Vehiculo, callback: (Result<Unit>) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dataManager.updateVehiculo(vehiculo)
                }
                callback(Result.success(Unit))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    fun deleteVehiculo(vehiculo: Vehiculo, callback: (Result<Unit>) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dataManager.deleteVehiculo(vehiculo)
                }
                callback(Result.success(Unit))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    fun getVehiculosCount(callback: (Int) -> Unit) {
        scope.launch {
            val count = withContext(Dispatchers.IO) {
                dataManager.getVehiculosCount()
            }
            callback(count)
        }
    }

    fun deleteAllVehiculos(callback: (Result<Unit>) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dataManager.deleteAllVehiculos()
                }
                callback(Result.success(Unit))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
}
