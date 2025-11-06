package util

import util.Constants

object ValidationUtils {

    fun validarMarca(marca: String): String? {
        if (marca.isBlank()) return "La marca es obligatoria"
        if (marca.length < 2) return "La marca debe tener al menos 2 caracteres"
        return null
    }

    fun validarModelo(modelo: String): String? {
        if (modelo.isBlank()) return "El modelo es obligatorio"
        if (modelo.length < 2) return "El modelo debe tener al menos 2 caracteres"
        return null
    }

    fun validarAnio(anioStr: String): String? {
        if (anioStr.isBlank()) return "El año es obligatorio"
        val anio = anioStr.toIntOrNull()
        if (anio == null || anio !in Constants.MIN_YEAR..Constants.MAX_YEAR) {
            return "El año debe estar entre ${Constants.MIN_YEAR} y ${Constants.MAX_YEAR}"
        }
        return null
    }

    fun validarPrecio(precioStr: String): String? {
        if (precioStr.isBlank()) return "El precio es obligatorio"
        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            return "El precio debe ser mayor a 0"
        }
        return null
    }

    fun validarKilometraje(kmStr: String): String? {
        if (kmStr.isBlank()) return "El kilometraje es obligatorio"
        val km = kmStr.toIntOrNull()
        if (km == null || km < 0) {
            return "El kilometraje no puede ser negativo"
        }
        return null
    }

    fun validarDescripcion(descripcion: String): String? {
        if (descripcion.isBlank()) return "La descripción es obligatoria"
        if (descripcion.length < 10) return "La descripción debe tener al menos 10 caracteres"
        return null
    }
}