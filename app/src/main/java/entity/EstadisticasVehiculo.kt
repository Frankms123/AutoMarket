package Entity

import java.io.Serializable

data class EstadisticasVehiculo(
    val totalVehiculos: Int = 0,
    val vehiculosNuevos: Int = 0,
    val vehiculosUsados: Int = 0,
    val precioPromedio: Double = 0.0,
    val kilometrajePromedio: Int = 0,
    val anioPromedio: Int = 0
) : Serializable {

    fun getPorcentajeNuevos(): Double {
        return if (totalVehiculos > 0) (vehiculosNuevos.toDouble() / totalVehiculos) * 100 else 0.0
    }

    fun getPorcentajeUsados(): Double {
        return if (totalVehiculos > 0) (vehiculosUsados.toDouble() / totalVehiculos) * 100 else 0.0
    }
}