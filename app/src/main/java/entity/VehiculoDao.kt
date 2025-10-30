package entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface VehiculoDao {
    @Query("SELECT * FROM Vehiculo ORDER BY fechaCreacion DESC")
    fun getAllVehiculos(): Flow<List<Vehiculo>>

    @Query("SELECT * FROM Vehiculo WHERE id = :id")
    suspend fun getVehiculoById(id: Long): Vehiculo?

    @Query("SELECT * FROM Vehiculo WHERE marca LIKE '%' || :query || '%' OR modelo LIKE '%' || :query || '%'")
    fun searchVehiculos(query: String): Flow<List<Vehiculo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehiculo(vehiculo: Vehiculo): Long

    @Update
    suspend fun updateVehiculo(vehiculo: Vehiculo)

    @Delete
    suspend fun deleteVehiculo(vehiculo: Vehiculo)

    @Query("DELETE FROM Vehiculo")
    suspend fun deleteAllVehiculos()

    @Query("SELECT COUNT(*) FROM Vehiculo")
    suspend fun getVehiculosCount(): Int
}