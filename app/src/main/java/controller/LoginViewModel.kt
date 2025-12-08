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

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: UserDto) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginUiState.Error("Por favor, ingresa un correo electrónico válido.")
            return
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("La contraseña no puede estar vacía.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val resource = safeApiCall { authRepository.login(email, password) }) {
                is Resource.Success -> {
                    _uiState.value = LoginUiState.Success(resource.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = LoginUiState.Error(resource.message ?: "Email o contraseña incorrectos.")
                }
            }
        }
    }
}