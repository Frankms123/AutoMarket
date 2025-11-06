package Entity

enum class OrdenVehiculo {
    PRECIO_ASC,      // Precio: menor a mayor
    PRECIO_DESC,     // Precio: mayor a menor
    ANIO_ASC,        // Año: más antiguo a más nuevo
    ANIO_DESC,       // Año: más nuevo a más antiguo
    KILOMETRAJE_ASC, // Kilometraje: menor a mayor
    KILOMETRAJE_DESC,// Kilometraje: mayor a menor
    FECHA_ASC,       // Fecha publicación: más antiguo
    FECHA_DESC       // Fecha publicación: más reciente (default)
}
