package controller

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.repository.AuthRepository
import entity.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import util.Resource
import util.safeApiCall

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: UserDto) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(email: String, password: String, confirmPassword: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = RegisterUiState.Error("Por favor, ingresa un correo electrónico válido.")
            return
        }
        if (password.length < 6) {
            _uiState.value = RegisterUiState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error("Las contraseñas no coinciden.")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            when (val resource = safeApiCall { authRepository.register(email, password) }) {
                is Resource.Success -> {
                    _uiState.value = RegisterUiState.Success(resource.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = RegisterUiState.Error(resource.message ?: "No se pudo crear la cuenta. Es posible que el correo ya esté en uso.")
                }
            }
        }
    }
}