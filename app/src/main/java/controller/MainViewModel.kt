package controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.repository.VehicleRepository
import entity.VehicleFilter
import entity.VehicleDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import util.Resource
import util.safeApiCall

enum class SortOrder {
    PRICE_ASC, PRICE_DESC, YEAR_DESC, YEAR_ASC, MILEAGE_ASC, MILEAGE_DESC
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val vehicles: List<VehicleDto>, val filter: VehicleFilter) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel : ViewModel() {

    private val vehicleRepository = VehicleRepository()

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    private var masterVehicleList = listOf<VehicleDto>()
    private var currentFilter = VehicleFilter()
    var searchQuery = ""
        private set
    private var searchJob: Job? = null
    private var sortOrder = SortOrder.YEAR_DESC // Default sort order
    private var isInitialLoad = true

    init {
        loadVehicles()
    }

    fun loadVehicles() {
        viewModelScope.launch {
            if (isInitialLoad) {
                _uiState.value = MainUiState.Loading
            }
            when (val resource = safeApiCall { vehicleRepository.getVehicles() }) {
                is Resource.Success -> {
                    masterVehicleList = resource.data ?: emptyList()
                    _updateList()
                    isInitialLoad = false
                }
                is Resource.Error -> {
                    if(isInitialLoad) {
                        _uiState.value = MainUiState.Error(resource.message ?: "Error loading vehicles")
                    }
                }
            }
        }
    }

    fun applyFilters(filter: VehicleFilter) {
        currentFilter = filter
        _updateList()
    }

    fun searchVehicles(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            _updateList()
        }
    }

    fun sortVehicles(order: SortOrder) {
        sortOrder = order
        _updateList()
    }

    private fun _updateList() {
        var filteredVehicles = masterVehicleList

        if (searchQuery.isNotBlank()) {
            filteredVehicles = filteredVehicles.filter { vehicle ->
                vehicle.brand.contains(searchQuery, ignoreCase = true) ||
                vehicle.model.contains(searchQuery, ignoreCase = true)
            }
        }

        filteredVehicles = filteredVehicles.filter { vehicle ->
            (currentFilter.vehicleType == null || vehicle.type.equals(currentFilter.vehicleType, ignoreCase = true)) &&
            (currentFilter.minPrice == null || vehicle.price >= currentFilter.minPrice!!) &&
            (currentFilter.maxPrice == null || vehicle.price <= currentFilter.maxPrice!!) &&
            (currentFilter.minYear == null || vehicle.year >= currentFilter.minYear!!) &&
            (currentFilter.maxYear == null || vehicle.year <= currentFilter.maxYear!!) &&
            (currentFilter.maxMileage == null || vehicle.mileage <= currentFilter.maxMileage!!) &&
            (currentFilter.transmission == null || vehicle.transmission.equals(currentFilter.transmission, ignoreCase = true)) &&
            (currentFilter.condition == null || vehicle.condition.equals(currentFilter.condition, ignoreCase = true))
        }

        val sortedVehicles = when (sortOrder) {
            SortOrder.PRICE_ASC -> filteredVehicles.sortedBy { it.price }
            SortOrder.PRICE_DESC -> filteredVehicles.sortedByDescending { it.price }
            SortOrder.YEAR_DESC -> filteredVehicles.sortedByDescending { it.year }
            SortOrder.YEAR_ASC -> filteredVehicles.sortedBy { it.year }
            SortOrder.MILEAGE_ASC -> filteredVehicles.sortedBy { it.mileage }
            SortOrder.MILEAGE_DESC -> filteredVehicles.sortedByDescending { it.mileage }
        }

        _uiState.value = MainUiState.Success(sortedVehicles, currentFilter)
    }
}