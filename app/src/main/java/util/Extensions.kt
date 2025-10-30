package util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    action: () -> Unit
) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setAction(actionText) { action() }
        .show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.isInvisible(): Boolean = visibility == View.INVISIBLE


fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}


fun String.isValidPrice(): Boolean {
    return this.toDoubleOrNull()?.let { it > 0 } ?: false
}

fun String.isValidYear(): Boolean {
    return this.toIntOrNull()?.let { it in Constants.MIN_YEAR..Constants.MAX_YEAR } ?: false
}

fun String.isValidKilometraje(): Boolean {
    return this.toIntOrNull()?.let { it >= 0 } ?: false
}

fun String.isNotNullOrBlank(): Boolean {
    return !this.isNullOrBlank()
}

fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}


fun Double.formatAsPrecio(): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this)
}

fun Int.formatAsKilometraje(): String {
    return "${NumberFormat.getInstance().format(this)} km"
}

fun Long.formatAsDate(): String {
    val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
    return sdf.format(Date(this))
}
