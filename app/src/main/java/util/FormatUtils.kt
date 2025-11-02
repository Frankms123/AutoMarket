package util
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CR"))
        return format.format(price)
    }

    fun formatKilometraje(km: Int): String {
        return "${NumberFormat.getNumberInstance(Locale.US).format(km)} km"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}