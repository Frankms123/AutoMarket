package com.Frank.automarket

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.Frank.automarket.databinding.ActivityFormularioVehiculoBinding
import Entity.ResultadoOperacion
import Entity.Vehiculo
import controller.VehiculoController
import util.ImageUtils
import util.ValidationUtils
import util.toast
import util.visible
import util.gone
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File

class FormularioVehiculoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioVehiculoBinding
    private lateinit var controller: VehiculoController

    private var vehiculoId: Long = -1
    private var modoEdicion: Boolean = false
    private var imagenUri: Uri? = null
    private var imagenPath: String? = null

    private val tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) guardarImagenYMostrar()
    }

    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imagenUri = uri
            guardarImagenYMostrar()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) abrirCamara() else toast(getString(R.string.error_permiso_camara))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioVehiculoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupController()
        setupSpinners()
        obtenerDatosIntent()
        setupListeners()

        if (modoEdicion) cargarDatosVehiculo()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (modoEdicion) getString(R.string.titulo_editar) else getString(R.string.titulo_agregar)
    }

    private fun setupController() {
        controller = VehiculoController(this)
    }

    private fun setupSpinners() {
        val tiposVehiculo = resources.getStringArray(R.array.tipos_vehiculo)
        binding.spinnerTipoVehiculo.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposVehiculo))

        val tiposTransmision = resources.getStringArray(R.array.tipos_transmision)
        binding.spinnerTransmision.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposTransmision))

        val estados = resources.getStringArray(R.array.estados_vehiculo)
        binding.spinnerEstado.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados))
    }

    private fun obtenerDatosIntent() {
        vehiculoId = intent.getLongExtra(EXTRA_VEHICULO_ID, -1)
        modoEdicion = intent.getBooleanExtra(EXTRA_MODO_EDICION, false)
    }

    private fun setupListeners() {
        binding.btnSeleccionarImagen.setOnClickListener { mostrarDialogoSeleccionImagen() }
        binding.btnGuardar.setOnClickListener { if (validarFormulario()) guardarVehiculo() }
        binding.btnCancelar.setOnClickListener { mostrarDialogoSalir() }
    }

    private fun cargarDatosVehiculo() {
        lifecycleScope.launch {
            when (val resultado = controller.obtenerVehiculoPorId(vehiculoId)) {
                is ResultadoOperacion.Exito -> resultado.data?.let { llenarFormulario(it) }
                is ResultadoOperacion.Error -> {
                    toast(resultado.mensaje)
                    finish()
                }
                else -> {}
            }
        }
    }

    private fun llenarFormulario(vehiculo: Vehiculo) {
        binding.apply {
            etMarca.setText(vehiculo.marca)
            etModelo.setText(vehiculo.modelo)
            etAnio.setText(vehiculo.anio.toString())
            etPrecio.setText(vehiculo.precio.toString())
            etKilometraje.setText(vehiculo.kilometraje.toString())
            etDescripcion.setText(vehiculo.descripcion)
            spinnerTipoVehiculo.setText(vehiculo.tipoVehiculo, false)
            spinnerTransmision.setText(vehiculo.transmision, false)
            spinnerEstado.setText(vehiculo.estado, false)
            imagenPath = vehiculo.imagenUri
            if (vehiculo.tieneImagen()) mostrarImagenPreview()
        }
    }

    private fun mostrarDialogoSeleccionImagen() {
        val items = resources.getStringArray(R.array.opciones_imagen)
        val listener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> verificarPermisoYAbrirCamara()
                1 -> abrirGaleria()
                2 -> eliminarImagen()
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.btn_seleccionar_imagen)
            .setItems(items, listener)
            .show()
    }

    private fun verificarPermisoYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        try {
            val photoFile = File.createTempFile("VEHICLE_${System.currentTimeMillis()}", ".jpg", cacheDir)
            imagenUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            tomarFotoLauncher.launch(imagenUri)
        } catch (e: Exception) {
            toast(getString(R.string.error_abrir_camara))
        }
    }

    private fun abrirGaleria() {
        seleccionarImagenLauncher.launch("image/*")
    }

    private fun guardarImagenYMostrar() {
        imagenUri?.let { uri ->
            imagenPath = ImageUtils.guardarImagenEnStorage(this, uri)
            if (imagenPath != null) {
                mostrarImagenPreview()
                toast(getString(R.string.msg_imagen_guardada))
            } else {
                toast(getString(R.string.error_guardar_imagen))
            }
        }
    }

    private fun mostrarImagenPreview() {
        binding.ivPreview.visible()
        imagenPath?.let { path ->
            Glide.with(this).load(File(path)).centerCrop().into(binding.ivPreview)
        }
    }

    private fun eliminarImagen() {
        imagenPath?.let { ImageUtils.eliminarImagen(it) }
        imagenPath = null
        imagenUri = null
        binding.ivPreview.gone()
        toast(getString(R.string.msg_imagen_eliminada))
    }

    private fun validarFormulario(): Boolean {
        binding.apply {
            ValidationUtils.validarMarca(etMarca.text.toString()).also { tilMarca.error = it }?.let { return false }
            ValidationUtils.validarModelo(etModelo.text.toString()).also { tilModelo.error = it }?.let { return false }
            ValidationUtils.validarAnio(etAnio.text.toString()).also { tilAnio.error = it }?.let { return false }
            ValidationUtils.validarPrecio(etPrecio.text.toString()).also { tilPrecio.error = it }?.let { return false }
            ValidationUtils.validarKilometraje(etKilometraje.text.toString()).also { tilKilometraje.error = it }?.let { return false }
            ValidationUtils.validarDescripcion(etDescripcion.text.toString()).also { tilDescripcion.error = it }?.let { return false }
            return true
        }
    }

    private fun guardarVehiculo() {
        val vehiculo = Vehiculo(
            id = if (modoEdicion) vehiculoId else 0,
            marca = binding.etMarca.text.toString().trim(),
            modelo = binding.etModelo.text.toString().trim(),
            anio = binding.etAnio.text.toString().toInt(),
            precio = binding.etPrecio.text.toString().toDouble(),
            tipoVehiculo = binding.spinnerTipoVehiculo.text.toString(),
            kilometraje = binding.etKilometraje.text.toString().toInt(),
            transmision = binding.spinnerTransmision.text.toString(),
            estado = binding.spinnerEstado.text.toString(),
            descripcion = binding.etDescripcion.text.toString().trim(),
            imagenUri = imagenPath
        )

        lifecycleScope.launch {
            val resultado = if (modoEdicion) controller.actualizarVehiculo(vehiculo) else controller.crearVehiculo(vehiculo)
            when (resultado) {
                is ResultadoOperacion.Exito -> {
                    toast(getString(if (modoEdicion) R.string.msg_vehiculo_actualizado else R.string.msg_vehiculo_guardado))
                    finish()
                }
                is ResultadoOperacion.Error -> toast(resultado.mensaje)
                else -> {}
            }
        }
    }

    private fun mostrarDialogoSalir() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialogo_salir_titulo)
            .setMessage(R.string.dialogo_salir_mensaje)
            .setPositiveButton(R.string.dialogo_salir_confirmar) { _, _ -> finish() }
            .setNegativeButton(R.string.dialogo_salir_cancelar, null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        mostrarDialogoSalir()
        return true
    }

    override fun onBackPressed() {
        mostrarDialogoSalir()
    }

    companion object {
        const val EXTRA_VEHICULO_ID = "vehiculo_id"
        const val EXTRA_MODO_EDICION = "modo_edicion"
    }
}