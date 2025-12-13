package com.Frank.automarket

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.Frank.automarket.databinding.ActivityVehicleFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import controller.UiState
import controller.VehicleFormViewModel
import data.network.ApiClient
import data.session.SessionManager
import entity.VehicleDto
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import util.*
import java.io.File

class VehicleFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleFormBinding
    private val viewModel: VehicleFormViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var vehicleId: Int? = null
    private var imageFile: File? = null
    private var imageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) saveImageAndShow()
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            saveImageAndShow()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera() else toast(getString(R.string.error_camera_permission))
    }

    private val requestGalleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openGalleryInternal() else toast(getString(R.string.error_storage_permission))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupSpinners()
        setupListeners()
        observeViewModel()
        processIntent()
    }

    private fun processIntent() {
        val id = intent.getIntExtra(EXTRA_VEHICLE_ID, -1)
        if (id != -1) {
            vehicleId = id
            viewModel.loadVehicleForEditing(id)
            binding.btnSelectImage.gone()
        } else {
            supportActionBar?.title = getString(R.string.title_add)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupSpinners() {
        val vehicleTypes = resources.getStringArray(R.array.vehicle_types)
        binding.spinnerVehicleType.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, vehicleTypes))

        val transmissionTypes = resources.getStringArray(R.array.transmission_types)
        binding.spinnerTransmission.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, transmissionTypes))

        val conditions = resources.getStringArray(R.array.vehicle_conditions)
        binding.spinnerCondition.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, conditions))
    }

    private fun setupListeners() {
        binding.btnSelectImage.setOnClickListener { showImageSelectionDialog() }
        binding.btnSave.setOnClickListener { saveVehicle() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar.visible()
                        binding.formContainer.gone()
                    }
                    is UiState.SuccessMessage -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                        toast(it.message)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    is UiState.Error -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                        toast("Error: ${it.message}")
                    }
                    is UiState.Success -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                        fillForm(it.data)
                    }
                    is UiState.Idle -> {
                        binding.progressBar.gone()
                        binding.formContainer.visible()
                    }
                }
            }
        }
    }

    private fun fillForm(vehicle: VehicleDto) {
        supportActionBar?.title = getString(R.string.title_edit)
        binding.apply {
            etBrand.setText(vehicle.brand)
            etModel.setText(vehicle.model)
            etYear.setText(vehicle.year.toString())
            etPrice.setText(vehicle.price.toString())
            etMileage.setText(vehicle.mileage.toString())
            etDescription.setText(vehicle.description)
            spinnerVehicleType.setText(vehicle.type, false)
            spinnerTransmission.setText(vehicle.transmission, false)
            spinnerCondition.setText(vehicle.condition, false)

            if (vehicle.imageUrl != null) {
                ivPreview.visible()
                val fullImageUrl = ApiClient.BASE_URL.removeSuffix("/") + vehicle.imageUrl
                Glide.with(this@VehicleFormActivity).load(fullImageUrl).centerCrop().into(ivPreview)
            }
        }
    }

    private fun saveVehicle() {
        if (!validateForm()) return

        val ownerId = sessionManager.fetchUserId()
        if (ownerId == -1) {
            toast("Session error. Please restart the application.")
            return
        }

        val year = binding.etYear.text.toString().toIntOrNull()
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        val mileage = binding.etMileage.text.toString().toIntOrNull()

        if (year == null || price == null || mileage == null) {
            toast("Please check the numeric fields.")
            return
        }

        viewModel.saveVehicle(
            vehicleId = vehicleId,
            brand = binding.etBrand.text.toString().trim(),
            model = binding.etModel.text.toString().trim(),
            year = year,
            price = price,
            mileage = mileage,
            description = binding.etDescription.text.toString().trim(),
            type = binding.spinnerVehicleType.text.toString(),
            transmission = binding.spinnerTransmission.text.toString(),
            condition = binding.spinnerCondition.text.toString(),
            ownerId = ownerId,
            imageFile = imageFile
        )
    }

    private fun validateForm(): Boolean {
        binding.tilBrand.error = ValidationUtils.validateBrand(binding.etBrand.text.toString())
        binding.tilModel.error = ValidationUtils.validateModel(binding.etModel.text.toString())
        binding.tilYear.error = ValidationUtils.validateYear(binding.etYear.text.toString())
        binding.tilPrice.error = ValidationUtils.validatePrice(binding.etPrice.text.toString())
        binding.tilMileage.error = ValidationUtils.validateMileage(binding.etMileage.text.toString())

        if (imageFile == null && vehicleId == null) {
            toast("Please select an image.")
            return false
        }

        return listOf(
            binding.tilBrand.error,
            binding.tilModel.error,
            binding.tilYear.error,
            binding.tilPrice.error,
            binding.tilMileage.error
        ).all { it == null }
    }

    private fun showImageSelectionDialog() {
        val items = resources.getStringArray(R.array.image_options)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.btn_select_image)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> checkGalleryPermission()
                    2 -> deleteImage()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGalleryInternal()
        } else {
            requestGalleryPermissionLauncher.launch(permission)
        }
    }

    private fun openCamera() {
        try {
            imageFile = ImageUtils.createImageFile(this)
            imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile!!)
            takePictureLauncher.launch(imageUri)
        } catch (_: Exception) {
            toast(getString(R.string.error_open_camera))
        }
    }

    private fun openGalleryInternal() {
        selectImageLauncher.launch("image/*")
    }

    private fun saveImageAndShow() {
        imageUri?.let {
            imageFile = ImageUtils.getFileFromUri(this, it)
            if (imageFile != null) {
                showImagePreview(it)
                toast(getString(R.string.msg_image_ready_to_upload))
            } else {
                toast(getString(R.string.error_processing_image))
            }
        }
    }

    private fun showImagePreview(uri: Uri) {
        binding.ivPreview.visible()
        Glide.with(this).load(uri).centerCrop().into(binding.ivPreview)
    }

    private fun deleteImage() {
        imageFile = null
        imageUri = null
        binding.ivPreview.gone()
        toast(getString(R.string.msg_image_deleted))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_VEHICLE_ID = "vehicle_id"
    }
}
