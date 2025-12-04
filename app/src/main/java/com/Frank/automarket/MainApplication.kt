package com.Frank.automarket

import android.app.Application
import com.Frank.automarket.data.network.ApiClient

/**
 * Clase Application personalizada para inicializar componentes globales.
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializa nuestro ApiClient con el contexto de la aplicación.
        // Esto asegura que solo haya una instancia para toda la app.
        ApiClient.initialize(this)
    }
}

private fun ApiClient.initialize(application: MainApplication) {}
