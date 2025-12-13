package util

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    action: () -> Unit
): Snackbar {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setAction(actionText) { action() }
    snackbar.show()
    return snackbar
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}


fun Double.formatAsPrice(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(this)
}
fun Int.formatAsMileage(): String {
    return "${NumberFormat.getInstance().format(this)} km"
}
