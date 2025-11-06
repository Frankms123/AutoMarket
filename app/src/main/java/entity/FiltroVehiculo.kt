package Entity

import java.io.Serializable

data class FiltroVehiculo(
    val tipoVehiculo: String? = null,
    val precioMin: Double? = null,
    val precioMax: Double? = null,
    val anioMin: Int? = null,
    val anioMax: Int? = null,
    val kilometrajeMax: Int? = null,
    val transmision: String? = null,
    val estado: String? = null
) : Serializable {

    fun tienesFiltrosActivos(): Boolean {
        return tipoVehiculo != null ||
                precioMin != null ||
                precioMax != null ||
                anioMin != null ||
                anioMax != null ||
                kilometrajeMax != null ||
                transmision != null ||
                estado != null
    }

    fun limpiar(): FiltroVehiculo = FiltroVehiculo()
}
