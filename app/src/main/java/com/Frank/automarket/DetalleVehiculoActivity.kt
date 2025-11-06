package com.Frank.automarket

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.Frank.automarket.databinding.ActivityDetalleVehiculoBinding
import Entity.ResultadoOperacion
import Entity.Vehiculo
import controller.VehiculoController
import util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File

class DetalleVehiculoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDetalleVehiculoBinding
    private lateinit var controller: VehiculoController
    private var vehiculoActual: Vehiculo? = null
    private var vehiculoId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleVehiculoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupController()
        obtenerVehiculoId()
        cargarVehiculo()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.titulo_detalle)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupController() {
        controller = VehiculoController(this)
    }
    
    private fun obtenerVehiculoId() {
        vehiculoId = intent.getLongExtra(MainActivity.EXTRA_VEHICULO_ID, -1)
        
        if (vehiculoId == -1L) {
            toast(getString(R.string.error_cargar_vehiculo))
            finish()
        }
    }
    
    private fun cargarVehiculo() {
        lifecycleScope.launch {
            when (val resultado = controller.obtenerVehiculoPorId(vehiculoId)) {
                is ResultadoOperacion.Exito -> {
                    resultado.data?.let { vehiculo ->
                        vehiculoActual = vehiculo
                        mostrarDatosVehiculo(vehiculo)
                    } ?: run {
                        toast(getString(R.string.error_cargar_vehiculo))
                        finish()
                    }
                }
                is ResultadoOperacion.Error -> {
                    toast(resultado.mensaje)
                    finish()
                }
                else -> { }
            }
        }
    }
    
    private fun mostrarDatosVehiculo(vehiculo: Vehiculo) {
        binding.apply {
            tvTitulo.text = vehiculo.getTituloCompleto()
            tvPrecio.text = vehiculo.getPrecioFormateado()
            tvEstado.text = vehiculo.estado
            tvEstado.setBackgroundColor(
                if (vehiculo.esNuevo()) getColor(R.color.badge_nuevo) else getColor(R.color.badge_usado)
            )
            
            tvAnio.text = vehiculo.anio.toString()
            tvTipo.text = vehiculo.tipoVehiculo
            tvKilometraje.text = vehiculo.getKilometrajeFormateado()
            tvTransmision.text = vehiculo.transmision
            tvFechaPublicacion.text = vehiculo.fechaCreacion.formatAsDate()
            tvDescripcion.text = vehiculo.descripcion
            
            cargarImagen(vehiculo)
        }
    }
    
    private fun cargarImagen(vehiculo: Vehiculo) {
        if (vehiculo.tieneImagen()) {
            val imageFile = File(vehiculo.imagenUri!!)
            Glide.with(this@DetalleVehiculoActivity)
                .load(imageFile)
                .centerCrop()
                .placeholder(R.drawable.ic_car_placeholder)
                .error(R.drawable.ic_car_placeholder)
                .into(binding.ivVehiculo)
        } else {
            binding.ivVehiculo.setImageResource(R.drawable.ic_car_placeholder)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalle, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_edit -> { 
                val intent = Intent(this, FormularioVehiculoActivity::class.java).apply {
                    putExtra(FormularioVehiculoActivity.EXTRA_VEHICULO_ID, vehiculoId)
                    putExtra(FormularioVehiculoActivity.EXTRA_MODO_EDICION, true)
                }
                startActivity(intent)
                true 
            }
            R.id.action_delete -> { mostrarDialogoEliminar(); true }
            R.id.action_share -> { compartirVehiculo(); true }
            else -> super.onOptionsItemSelected(item)
        }
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
        vehiculoActual?.let { vehiculo ->
            lifecycleScope.launch {
                when (val resultado = controller.eliminarVehiculo(vehiculo)) {
                    is ResultadoOperacion.Exito -> {
                        toast(getString(R.string.msg_vehiculo_eliminado))
                        finish()
                    }
                    is ResultadoOperacion.Error -> toast(resultado.mensaje)
                    else -> { }
                }
            }
        }
    }
    
    private fun compartirVehiculo() {
        vehiculoActual?.let { vehiculo ->
            val textoCompartir = "¡Mira este ${vehiculo.getTituloCompleto()}! Precio: ${vehiculo.getPrecioFormateado()}"
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Vehículo en venta")
                putExtra(Intent.EXTRA_TEXT, textoCompartir)
            }
            
            startActivity(Intent.createChooser(intent, "Compartir vía"))
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (vehiculoId != -1L) {
            cargarVehiculo()
        }
    }
}