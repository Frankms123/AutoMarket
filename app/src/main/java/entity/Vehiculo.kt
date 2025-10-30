package entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehiculo")
data class Vehiculo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val precio: Double,
    val kilometraje: Int,
    val transmision: String,
    val estado: String,
    val descripcion: String,
    val imagen: String?,
    val fechaCreacion: Long = System.currentTimeMillis()
)