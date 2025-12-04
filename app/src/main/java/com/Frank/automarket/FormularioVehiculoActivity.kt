package com.Frank.automarket

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.Frank.automarket.data.network.model.VehiculoDto
import com.Frank.automarket.data.session.SessionManager
import com.Frank.automarket.databinding.ActivityFormularioVehiculoBinding
import com.Frank.automarket.ui.form.FormularioUiState
import com.Frank.automarket.ui.form.FormularioVehiculoViewModel
import util.ImageUtils
import util.toast
import util.visible
import util.gone
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class FormularioVehiculoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioVehiculoBinding
    private val viewModel: FormularioVehiculoViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var vehiculoId: Int? = null
    private var imageFile: File? = null
    private var imagenUri: Uri? = null

    // --- ActivityResultLaunchers ---
    private val tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) guardarImagenYMostrar()
    }

    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imagenUri = it
            guardarImagenYMostrar()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) abrirCamara() else toast(getString(R.string.error_permiso_camara))
    }

    private val requestGalleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) abrirGaleriaInterno() else toast(getString(R.string.error_permiso_almacenamiento))
    }
    // --- Fin ActivityResultLaunchers ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioVehiculoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupSpinners()
        setupListeners()
        observeViewModel()
        procesarIntent()
    }

    private fun procesarIntent() {
        val id = intent.getIntExtra(EXTRA_VEHICULO_ID, -1)
        if (id != -1) {
            vehiculoId = id
            viewModel.cargarVehiculoParaEdicion(id)
            binding.btnSeleccionarImagen.gone()
        } else {
            supportActionBar?.title = getString(R.string.titulo_agregar)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupSpinners() {
        val tiposVehiculo = resources.getStringArray(R.array.tipos_vehiculo)
        binding.spinnerTipoVehiculo.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposVehiculo))

        val tiposTransmision = resources.getStringArray(R.array.tipos_transmision)
        binding.spinnerTransmision.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposTransmision))

        val estados = resources.getStringArray(R.array.estados_vehiculo)
        binding.spinnerEstado.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados))
    }

    private fun setupListeners() {
        binding.btnSeleccionarImagen.setOnClickListener { mostrarDialogoSeleccionImagen() }
        binding.btnGuardar.setOnClickListener { guardarVehiculo() }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when (it) {
                    is FormularioUiState.Loading -> {
                        binding.progressBar.visible()
                        binding.formContainer.gone()
                    }
                    is FormularioUiState.Success -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                        toast(it.message)
                        finish()
                    }
                    is FormularioUiState.Error -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                        toast("Error: ${it.message}")
                    }
                    is FormularioUiState.VehiculoLoaded -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                        llenarFormulario(it.vehiculo)
                    }
                    is FormularioUiState.Idle -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                    }
                }
            }
        }
    }

    private fun llenarFormulario(vehiculo: VehiculoDto) {
        supportActionBar?.title = getString(R.string.titulo_editar)
        binding.apply {
            etMarca.setText(vehiculo.marca)
            etModelo.setText(vehiculo.modelo)
            etAnio.setText(vehiculo.anio.toString())
            etPrecio.setText(vehiculo.precio.toString())
            etKilometraje.setText(vehiculo.kilometraje.toString())
            etDescripcion.setText(vehiculo.descripcion)
            spinnerTipoVehiculo.setText(vehiculo.tipo, false)
            spinnerTransmision.setText(vehiculo.transmision, false)
            spinnerEstado.setText(vehiculo.estado, false)

            if (vehiculo.imagenUrl != null) {
                ivPreview.visible()
                Glide.with(this@FormularioVehiculoActivity).load(vehiculo.imagenUrl).centerCrop().into(ivPreview)
            }
        }
    }

    private fun guardarVehiculo() {
        if (!validarFormulario()) return

        val ownerId = sessionManager.fetchUserId()
        if (ownerId == -1) {
            toast("Error de sesión. Por favor, reinicie la aplicación.")
            return
        }

        val anio = binding.etAnio.text.toString().toIntOrNull()
        val precio = binding.etPrecio.text.toString().toDoubleOrNull()
        val kilometraje = binding.etKilometraje.text.toString().toIntOrNull()

        if (anio == null || precio == null || kilometraje == null) {
            toast("Por favor, revise los campos numéricos.")
            return
        }

        viewModel.guardarVehiculo(
            vehiculoId = vehiculoId,
            marca = binding.etMarca.text.toString().trim(),
            modelo = binding.etModelo.text.toString().trim(),
            anio = anio,
            precio = precio,
            kilometraje = kilometraje,
            descripcion = binding.etDescripcion.text.toString().trim(),
            tipo = binding.spinnerTipoVehiculo.text.toString(),
            transmision = binding.spinnerTransmision.text.toString(),
            estado = binding.spinnerEstado.text.toString(),
            ownerId = ownerId,
            imageFile = imageFile
        )
    }

    private fun validarFormulario(): Boolean {
        var esValido = true

        // Validación de campos de texto
        if (binding.etMarca.text.isNullOrBlank()) {
            binding.tilMarca.error = "La marca es requerida"
            esValido = false
        } else {
            binding.tilMarca.error = null
        }

        if (binding.etModelo.text.isNullOrBlank()) {
            binding.tilModelo.error = "El modelo es requerido"
            esValido = false
        } else {
            binding.tilModelo.error = null
        }
        
        // Validación de spinners
        if (binding.spinnerTipoVehiculo.text.isNullOrBlank()) {
            toast("Por favor, seleccione un tipo de vehículo.")
            esValido = false
        } 

        if (binding.spinnerTransmision.text.isNullOrBlank()) {
            toast("Por favor, seleccione un tipo de transmisión.")
            esValido = false
        }

        if (binding.spinnerEstado.text.isNullOrBlank()) {
            toast("Por favor, seleccione el estado del vehículo.")
            esValido = false
        }

        // Resto de validaciones...

        return esValido
    }

    // --- Manejo de Imagen ---

    private fun mostrarDialogoSeleccionImagen() {
        val items = resources.getStringArray(R.array.opciones_imagen)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.btn_seleccionar_imagen)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamara()
                    1 -> verificarPermisoGaleria()
                    2 -> eliminarImagen()
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun verificarPermisoGaleria() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            abrirGaleriaInterno()
        } else {
            requestGalleryPermissionLauncher.launch(permission)
        }
    }

    private fun abrirCamara() {
        try {
            imageFile = ImageUtils.createImageFile(this)
            imagenUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile!!)
            tomarFotoLauncher.launch(imagenUri)
        } catch (e: Exception) {
            toast(getString(R.string.error_abrir_camara))
        }
    }

    private fun abrirGaleriaInterno() {
        seleccionarImagenLauncher.launch("image/*")
    }

    private fun guardarImagenYMostrar() {
        imagenUri?.let {
            imageFile = ImageUtils.getFileFromUri(this, it)
            if (imageFile != null) {
                mostrarImagenPreview(it)
                toast(getString(R.string.msg_imagen_lista_para_subir))
            } else {
                toast(getString(R.string.error_procesar_imagen))
            }
        }
    }

    private fun mostrarImagenPreview(uri: Uri) {
        binding.ivPreview.visible()
        Glide.with(this).load(uri).centerCrop().into(binding.ivPreview)
    }

    private fun eliminarImagen() {
        imageFile = null
        imagenUri = null
        binding.ivPreview.gone()
        toast(getString(R.string.msg_imagen_eliminada))
    }
    // --- Fin Manejo de Imagen ---

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_VEHICULO_ID = "vehiculo_id"
    }
}