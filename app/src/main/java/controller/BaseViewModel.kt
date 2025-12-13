package controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import util.Resource
import util.safeApiCall

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class SuccessMessage(val message: String) : UiState<Nothing>()
    data class Error(val message: String) : UiState<Nothing>()
}

abstract class BaseViewModel<T> : ViewModel() {

    protected val _uiState = MutableStateFlow<UiState<T>>(UiState.Idle)
    val uiState: StateFlow<UiState<T>> = _uiState

    protected fun executeLoadCall(
        apiCall: suspend () -> Response<T>,
        errorMessage: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val resource = safeApiCall { apiCall() }) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(resource.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(resource.message ?: errorMessage)
                }
            }
        }
    }

    protected fun <R> executeActionCall(
        apiCall: suspend () -> Response<R>,
        successMessage: String,
        errorMessage: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val resource = safeApiCall { apiCall() }) {
                is Resource.Success -> {
                    _uiState.value = UiState.SuccessMessage(successMessage)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(resource.message ?: errorMessage)
                }
            }
        }
    }
}
