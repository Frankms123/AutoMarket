package com.Frank.automarket

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.Frank.automarket.data.network.ApiClient
import com.Frank.automarket.data.network.model.VehiculoDto
import com.Frank.automarket.data.session.SessionManager
import com.Frank.automarket.databinding.ActivityDetalleVehiculoBinding
import com.Frank.automarket.ui.detail.DetalleUiState
import com.Frank.automarket.ui.detail.DetalleVehiculoViewModel
import util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetalleVehiculoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleVehiculoBinding
    private val viewModel: DetalleVehiculoViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var vehiculoId: Int = -1
    private var vehiculoActual: VehiculoDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleVehiculoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        setupToolbar()
        obtenerVehiculoId()
        observeViewModel()

        if (vehiculoId != -1) {
            viewModel.cargarDetallesVehiculo(vehiculoId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.titulo_detalle)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun obtenerVehiculoId() {
        vehiculoId = intent.getIntExtra(EXTRA_VEHICULO_ID, -1)
        if (vehiculoId == -1) {
            toast(getString(R.string.error_cargar_vehiculo))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when(it) {
                    is DetalleUiState.Loading -> {
                        // TODO: Mostrar un ProgressBar
                    }
                    is DetalleUiState.Success -> {
                        vehiculoActual = it.vehiculo
                        mostrarDatosVehiculo(it.vehiculo)
                        invalidateOptionsMenu() // Invalida el menú para que onPrepareOptionsMenu se llame de nuevo
                    }
                    is DetalleUiState.Error -> {
                        toast("Error: ${it.message}")
                        finish()
                    }
                    is DetalleUiState.Deleted -> {
                        toast(getString(R.string.msg_vehiculo_eliminado))
                        finish()
                    }
                }
            }
        }
    }

    private fun mostrarDatosVehiculo(vehiculo: VehiculoDto) {
        binding.apply {
            tvTitulo.text = "${vehiculo.marca} ${vehiculo.modelo}"
            tvPrecio.text = FormatUtils.formatPrice(vehiculo.precio)
            tvEstado.text = vehiculo.estado

            tvAnio.text = vehiculo.anio.toString()
            tvTipo.text = vehiculo.tipo
            tvKilometraje.text = FormatUtils.formatKilometraje(vehiculo.kilometraje)
            tvTransmision.text = vehiculo.transmision
            tvDescripcion.text = vehiculo.descripcion
            
            if (vehiculo.imagenUrl != null) {
                val fullImageUrl = ApiClient.BASE_URL.removeSuffix("/") + vehiculo.imagenUrl
                Glide.with(this@DetalleVehiculoActivity)
                    .load(fullImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_car_placeholder)
                    .into(ivVehiculo)
            } else {
                ivVehiculo.setImageResource(R.drawable.ic_car_placeholder)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalle, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val esPropietario = vehiculoActual?.ownerId == sessionManager.fetchUserId()

        menu.findItem(R.id.action_edit)?.isVisible = esPropietario
        menu.findItem(R.id.action_delete)?.isVisible = esPropietario
        
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_edit -> { irAEditar(); true }
            R.id.action_delete -> { mostrarDialogoEliminar(); true }
            R.id.action_share -> { /* TODO: Implementar compartir */ true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun irAEditar() {
        val intent = Intent(this, FormularioVehiculoActivity::class.java).apply {
            putExtra(FormularioVehiculoActivity.EXTRA_VEHICULO_ID, vehiculoId)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoEliminar() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialogo_eliminar_titulo)
            .setMessage(R.string.dialogo_eliminar_mensaje)
            .setPositiveButton(R.string.dialogo_eliminar_confirmar) { _, _ -> eliminarVehiculo() }
            .setNegativeButton(R.string.dialogo_eliminar_cancelar, null)
            .show()
    }

    private fun eliminarVehiculo() {
        viewModel.eliminarVehiculo(vehiculoId)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_VEHICULO_ID = "vehiculo_id"
    }
}