package Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "vehiculos")
data class Vehiculo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val precio: Double,
    val tipoVehiculo: String,
    val kilometraje: Int,
    val transmision: String,
    val estado: String,
    val descripcion: String,
    val imagenUri: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
) : Serializable {

    fun esValido(): Boolean {
        return marca.isNotBlank() &&
                modelo.isNotBlank() &&
                anio in 1900..Calendar.getInstance().get(Calendar.YEAR) + 1 &&
                precio > 0 &&
                tipoVehiculo.isNotBlank() &&
                kilometraje >= 0 &&
                transmision.isNotBlank() &&
                estado.isNotBlank() &&
                descripcion.isNotBlank()
    }

    fun getTituloCompleto(): String = "$marca $modelo"

    fun getSubtitulo(): String = "$anio • $tipoVehiculo"

    fun esNuevo(): Boolean = estado.equals("Nuevo", ignoreCase = true)

    fun tieneImagen(): Boolean = !imagenUri.isNullOrBlank()

    fun getPrecioFormateado(): String = NumberFormat.getCurrencyInstance(Locale.US).format(precio)

    fun getKilometrajeFormateado(): String = "${NumberFormat.getInstance().format(kilometraje)} km"
}

object TipoVehiculo {
    const val SEDAN = "Sedán"
    const val SUV = "SUV"
    const val PICKUP = "Pickup"
    const val DEPORTIVO = "Deportivo"
    const val HATCHBACK = "Hatchback"
    const val VAN = "Van"
    fun obtenerTodos(): List<String> = listOf(SEDAN, SUV, PICKUP, DEPORTIVO, HATCHBACK, VAN)
}

object TipoTransmision {
    const val AUTOMATICO = "Automático"
    const val MANUAL = "Manual"
    fun obtenerTodos(): List<String> = listOf(AUTOMATICO, MANUAL)
}

object EstadoVehiculo {
    const val NUEVO = "Nuevo"
    const val USADO = "Usado"
    fun obtenerTodos(): List<String> = listOf(NUEVO, USADO)
}
