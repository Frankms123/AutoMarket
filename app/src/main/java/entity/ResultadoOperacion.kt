package Entity // Corregido para coincidir con la estructura

/**
 * Clase sellada para manejar resultados de operaciones
 * Permite un manejo elegante de éxito, error y carga
 */
sealed class ResultadoOperacion<out T> {
    /**
     * Operación exitosa con datos
     */
    data class Exito<T>(val data: T) : ResultadoOperacion<T>()
    
    /**
     * Operación fallida con mensaje de error
     */
    data class Error(
        val mensaje: String,
        val exception: Exception? = null
    ) : ResultadoOperacion<Nothing>()
    
    /**
     * Operación en progreso
     */
    object Cargando : ResultadoOperacion<Nothing>()
}

/**
 * Estados de la UI
 */
sealed class EstadoUI {
    object Inactivo : EstadoUI()
    object Cargando : EstadoUI()
    object Exito : EstadoUI()
    data class Error(val mensaje: String) : EstadoUI()
    object Vacio : EstadoUI()
}