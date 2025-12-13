package controller

import android.util.Patterns
import data.repository.AuthRepository
import entity.UserDto

class RegisterViewModel : BaseViewModel<UserDto>() {

    private val authRepository = AuthRepository()

    fun register(email: String, password: String, confirmPassword: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = UiState.Error("Por favor, ingresa un correo electrónico válido.")
            return
        }
        if (password.length < 6) {
            _uiState.value = UiState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = UiState.Error("Las contraseñas no coinciden.")
            return
        }

        executeLoadCall(
            apiCall = { authRepository.register(email, password) },
            errorMessage = "No se pudo crear la cuenta. Es posible que el correo ya esté en uso."
        )
    }
}
