package com.Frank.automarket.data.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona los datos de sesión del usuario, como el ID de usuario.
 * Versión simplificada para una arquitectura sin autenticación JWT.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Guarda el ID del usuario logueado.
     */
    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    /**
     * Obtiene el ID del usuario logueado.
     * Devuelve -1 si no se encuentra ningún usuario.
     */
    fun fetchUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    /**
     * Borra todos los datos de la sesión.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_USER_ID = "user_id"
    }
}