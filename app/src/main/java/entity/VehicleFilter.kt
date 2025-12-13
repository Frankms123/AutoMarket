package entity

import java.io.Serializable

data class VehicleFilter(
    val vehicleType: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val maxMileage: Int? = null,
    val transmission: String? = null,
    val condition: String? = null
) : Serializable {

    fun hasActiveFilters(): Boolean {
        return vehicleType != null ||
                minPrice != null ||
                maxPrice != null ||
                minYear != null ||
                maxYear != null ||
                maxMileage != null ||
                transmission != null ||
                condition != null
    }

    fun clear(): VehicleFilter = VehicleFilter()
}
