package controller

import android.util.Patterns
import data.repository.AuthRepository
import entity.UserDto

class LoginViewModel : BaseViewModel<UserDto>() {

    private val authRepository = AuthRepository()

    fun login(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = UiState.Error("Por favor, ingresa un correo electrónico válido.")
            return
        }
        if (password.isBlank()) {
            _uiState.value = UiState.Error("La contraseña no puede estar vacía.")
            return
        }

        executeLoadCall(
            apiCall = { authRepository.login(email, password) },
            errorMessage = "Email o contraseña incorrectos."
        )
    }
}
