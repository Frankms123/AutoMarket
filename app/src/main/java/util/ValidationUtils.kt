package util

object ValidationUtils {

    fun validateBrand(brand: String): String? {
        if (brand.isBlank()) return "Brand is required"
        return null
    }

    fun validateModel(model: String): String? {
        if (model.isBlank()) return "Model is required"
        if (model.length < 2) return "Model must be at least 2 characters long"
        return null
    }

    fun validateYear(yearStr: String): String? {
        if (yearStr.isBlank()) return "Year is required"
        val year = yearStr.toIntOrNull()
        if (year == null || year !in Constants.MIN_YEAR..Constants.MAX_YEAR) {
            return "Year must be between ${Constants.MIN_YEAR} and ${Constants.MAX_YEAR}"
        }
        return null
    }

    fun validatePrice(priceStr: String): String? {
        if (priceStr.isBlank()) return "Price is required"
        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            return "Price must be greater than 0"
        }
        return null
    }

    fun validateMileage(mileageStr: String): String? {
        if (mileageStr.isBlank()) return "Mileage is required"
        val mileage = mileageStr.toIntOrNull()
        if (mileage == null || mileage < 0) {
            return "Mileage can't be negative"
        }
        return null
    }

    fun validateDescription(description: String): String? {
        if (description.length < 10) {
            return "Description must be at least 10 characters long"
        }
        return null
    }
}
