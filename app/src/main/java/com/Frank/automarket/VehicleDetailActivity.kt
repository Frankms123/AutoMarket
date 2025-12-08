package com.Frank.automarket

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.Frank.automarket.databinding.ActivityVehicleDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import controller.DetailUiState
import controller.VehicleDetailViewModel
import data.network.ApiClient
import data.session.SessionManager
import entity.VehicleDto
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import util.*

class VehicleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleDetailBinding
    private val viewModel: VehicleDetailViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var vehicleId: Int = -1
    private var currentVehicle: VehicleDto? = null

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadVehicleDetails(vehicleId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        setupToolbar()
        getVehicleId()
        observeViewModel()

        if (vehicleId != -1) {
            viewModel.loadVehicleDetails(vehicleId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_detail)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun getVehicleId() {
        vehicleId = intent.getIntExtra(EXTRA_VEHICLE_ID, -1)
        if (vehicleId == -1) {
            toast(getString(R.string.error_loading_vehicle))
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when(it) {
                    is DetailUiState.Loading -> {
                        // You can show a ProgressBar here
                    }
                    is DetailUiState.Success -> {
                        currentVehicle = it.vehicle
                        displayVehicleData(it.vehicle)
                        invalidateOptionsMenu()
                    }
                    is DetailUiState.Error -> {
                        toast("Error: ${it.message}")
                        finish()
                    }
                    is DetailUiState.Deleted -> {
                        toast(getString(R.string.msg_vehicle_deleted))
                        setResult(RESULT_OK)
                        finish()
                    }
                    is DetailUiState.ShowUndoDelete -> {
                        binding.root.showSnackbarWithAction(getString(R.string.msg_vehicle_deleted), getString(R.string.btn_undo)) {
                            viewModel.undoDeletion()
                        }.addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    viewModel.confirmDeletion()
                                }                            }
                        })
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayVehicleData(vehicle: VehicleDto) {
        binding.apply {
            tvTitle.text = "${vehicle.brand} ${vehicle.model}"
            tvPrice.text = vehicle.price.formatAsPrice()
            tvCondition.text = vehicle.condition

            tvYear.text = vehicle.year.toString()
            tvType.text = vehicle.type
            tvMileage.text = vehicle.mileage.formatAsMileage()
            tvTransmission.text = vehicle.transmission
            tvDescription.text = vehicle.description
            
            if (vehicle.imageUrl != null) {
                val fullImageUrl = ApiClient.BASE_URL.removeSuffix("/") + vehicle.imageUrl
                Glide.with(this@VehicleDetailActivity)
                    .load(fullImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_car_placeholder)
                    .into(ivVehicle)
            } else {
                ivVehicle.setImageResource(R.drawable.ic_car_placeholder)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isOwner = currentVehicle?.ownerId == sessionManager.fetchUserId()
        menu.findItem(R.id.action_edit)?.isVisible = isOwner
        menu.findItem(R.id.action_delete)?.isVisible = isOwner
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_edit -> { goToEditScreen(); true }
            R.id.action_delete -> { showDeleteDialog(); true }
            R.id.action_share -> { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToEditScreen() {
        val intent = Intent(this, VehicleFormActivity::class.java).apply {
            putExtra(VehicleFormActivity.EXTRA_VEHICLE_ID, vehicleId)
        }
        activityLauncher.launch(intent)
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_message)
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ -> deleteVehicle() }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
    }

    private fun deleteVehicle() {
        currentVehicle?.let { viewModel.deleteVehicle(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_VEHICLE_ID = "vehicle_id"
    }
}
