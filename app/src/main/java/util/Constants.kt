package util

object Constants {
    val TRANSMISION_TYPES = listOf("Manual", "Automática")

    val ESTADO_TYPES = listOf("Nuevo", "Usado")

    const val MIN_YEAR = 1900
    const val MAX_YEAR = 2030
    const val MIN_PRICE = 0.0
    const val MIN_KILOMETRAJE = 0

    // Image
    const val MAX_IMAGE_SIZE_MB = 5
    const val IMAGE_QUALITY = 85

    const val REQUEST_CAMERA = 100
    const val REQUEST_GALLERY = 101

    const val PREFS_NAME = "AutoMarketPrefs"
    const val PREF_THEME = "theme"
    const val PREF_SORT_ORDER = "sort_order"
    const val DATE_FORMAT = "dd/MM/yyyy"
}
