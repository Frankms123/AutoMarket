package Entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoDao {
    @Query("SELECT * FROM vehiculos ORDER BY fechaCreacion DESC")
    fun getAllVehiculos(): Flow<List<Vehiculo>>

    @Query("SELECT * FROM vehiculos WHERE id = :id")
    suspend fun getVehiculoById(id: Long): Vehiculo?

    @Query("SELECT * FROM vehiculos WHERE marca LIKE '%' || :query || '%' OR modelo LIKE '%' || :query || '%'")
    fun searchVehiculos(query: String): Flow<List<Vehiculo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehiculo(vehiculo: Vehiculo): Long

    @Update
    suspend fun updateVehiculo(vehiculo: Vehiculo)

    @Delete
    suspend fun deleteVehiculo(vehiculo: Vehiculo)

    @Query("DELETE FROM vehiculos")
    suspend fun deleteAllVehiculos()

    @Query("SELECT COUNT(*) FROM vehiculos")
    suspend fun getVehiculosCount(): Int
}