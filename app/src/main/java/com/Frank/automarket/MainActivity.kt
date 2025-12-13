package com.Frank.automarket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.Frank.automarket.databinding.ActivityMainBinding
import com.Frank.automarket.databinding.DialogFilterBinding
import com.Frank.automarket.ui.adapters.VehicleAdapter
import com.Frank.automarket.ui.auth.LoginActivity
import controller.MainUiState
import controller.MainViewModel
import controller.SortOrder
import data.session.SessionManager
import entity.VehicleDto
import entity.VehicleFilter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var sessionManager: SessionManager
    private var lastFilter: VehicleFilter? = null

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadVehicles()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.title_list)
    }

    private fun setupRecyclerView() {
        vehicleAdapter = VehicleAdapter { vehicle: VehicleDto ->
            val intent = Intent(this, VehicleDetailActivity::class.java).apply {
                putExtra(VehicleDetailActivity.EXTRA_VEHICLE_ID, vehicle.id)
            }
            activityLauncher.launch(intent)
        }

        binding.rvVehicles.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = this@MainActivity.vehicleAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, VehicleFormActivity::class.java)
            activityLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is MainUiState.Loading -> {
                        binding.progressBar.visible()
                        binding.rvVehicles.gone()
                        binding.layoutEmpty.root.gone()
                    }
                    is MainUiState.Success -> {
                        binding.progressBar.gone()
                        if (state.vehicles.isEmpty()) {
                            val isSearchingOrFiltering = state.filter.hasActiveFilters() || viewModel.searchQuery.isNotEmpty()
                            val emptyTitle = if (isSearchingOrFiltering) getString(R.string.empty_state_search) else getString(R.string.empty_state_title)
                            val emptyMessage = if (isSearchingOrFiltering) getString(R.string.empty_state_search_message) else getString(R.string.empty_state_message)
                            showEmptyState(emptyTitle, emptyMessage)
                        } else {
                            showList()
                            vehicleAdapter.submitList(state.vehicles)
                        }
                        if (state.filter != lastFilter) {
                            lastFilter = state.filter
                            invalidateOptionsMenu()
                        }
                    }
                    is MainUiState.Error -> {
                        binding.progressBar.gone()
                        showEmptyState("Error", state.message)
                    }
                }
            }
        }
    }

    private fun showFilterDialog() {
        val dialogBinding = DialogFilterBinding.inflate(LayoutInflater.from(this))
        val currentState = viewModel.uiState.value
        val filter = if (currentState is MainUiState.Success) currentState.filter else VehicleFilter()

        // Setup spinners
        val vehicleTypes = resources.getStringArray(R.array.vehicle_types)
        dialogBinding.spinnerVehicleType.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, vehicleTypes))

        val transmissionTypes = resources.getStringArray(R.array.transmission_types)
        dialogBinding.spinnerTransmission.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, transmissionTypes))

        val conditions = resources.getStringArray(R.array.vehicle_conditions)
        dialogBinding.spinnerCondition.setAdapter(ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, conditions))

        // Set current filter values
        dialogBinding.etMinPrice.setText(filter.minPrice?.toString())
        dialogBinding.etMaxPrice.setText(filter.maxPrice?.toString())
        dialogBinding.etMinYear.setText(filter.minYear?.toString())
        dialogBinding.etMaxYear.setText(filter.maxYear?.toString())
        dialogBinding.etMaxMileage.setText(filter.maxMileage?.toString())
        dialogBinding.spinnerVehicleType.setText(filter.vehicleType, false)
        dialogBinding.spinnerTransmission.setText(filter.transmission, false)
        dialogBinding.spinnerCondition.setText(filter.condition, false)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.title_filters)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_apply) { _, _ ->
                val newFilter = VehicleFilter(
                    vehicleType = dialogBinding.spinnerVehicleType.text.toString().takeIf { it.isNotBlank() },
                    minPrice = dialogBinding.etMinPrice.text.toString().toDoubleOrNull(),
                    maxPrice = dialogBinding.etMaxPrice.text.toString().toDoubleOrNull(),
                    minYear = dialogBinding.etMinYear.text.toString().toIntOrNull(),
                    maxYear = dialogBinding.etMaxYear.text.toString().toIntOrNull(),
                    maxMileage = dialogBinding.etMaxMileage.text.toString().toIntOrNull(),
                    transmission = dialogBinding.spinnerTransmission.text.toString().takeIf { it.isNotBlank() },
                    condition = dialogBinding.spinnerCondition.text.toString().takeIf { it.isNotBlank() }
                )
                viewModel.applyFilters(newFilter)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .setNeutralButton(R.string.btn_clear) { _, _ ->
                viewModel.applyFilters(filter.clear())
            }
            .create()

        dialog.show()
    }

    private fun showEmptyState(title: String, message: String) {
        binding.apply {
            rvVehicles.gone()
            layoutEmpty.root.visible()
            layoutEmpty.tvEmptyTitle.text = title
            layoutEmpty.tvEmptyMessage.text = message
        }
    }

    private fun showList() {
        binding.apply {
            layoutEmpty.root.gone()
            rvVehicles.visible()
        }
    }

    private fun setupSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = getString(R.string.hint_search)

        if (viewModel.searchQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(viewModel.searchQuery, false) 
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false 
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty() && !searchItem.isActionViewExpanded) {
                    return true
                }
                viewModel.searchVehicles(newText.orEmpty())
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        setupSearchView(menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val filterItem = menu.findItem(R.id.action_filter)
        val currentState = viewModel.uiState.value
        if (currentState is MainUiState.Success && currentState.filter.hasActiveFilters()) {
            filterItem.setIcon(R.drawable.ic_filter_list_off)
        } else {
            filterItem.setIcon(android.R.drawable.ic_menu_sort_by_size)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> logout()
            R.id.action_filter -> showFilterDialog()
            R.id.action_sort_price_asc -> viewModel.sortVehicles(SortOrder.PRICE_ASC)
            R.id.action_sort_price_desc -> viewModel.sortVehicles(SortOrder.PRICE_DESC)
            R.id.action_sort_year_desc -> viewModel.sortVehicles(SortOrder.YEAR_DESC)
            R.id.action_sort_year_asc -> viewModel.sortVehicles(SortOrder.YEAR_ASC)
            R.id.action_sort_km_asc -> viewModel.sortVehicles(SortOrder.MILEAGE_ASC)
            R.id.action_sort_km_desc -> viewModel.sortVehicles(SortOrder.MILEAGE_DESC)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun logout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}