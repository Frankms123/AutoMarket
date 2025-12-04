package com.Frank.automarket.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Frank.automarket.data.network.model.UserDto
import com.Frank.automarket.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Representa los diferentes estados de la UI de la pantalla de Registro.
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: UserDto) : RegisterUiState() // Modificado
    data class Error(val message: String) : RegisterUiState()
}

/**
 * ViewModel para la RegisterActivity.
 */
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
            try {
                val response = authRepository.register(email, password)

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = RegisterUiState.Success(response.body()!!)
                } else {
                    _uiState.value = RegisterUiState.Error("No se pudo crear la cuenta. Es posible que el correo ya esté en uso.")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Ocurrió una excepción de red")
            }
        }
    }
}