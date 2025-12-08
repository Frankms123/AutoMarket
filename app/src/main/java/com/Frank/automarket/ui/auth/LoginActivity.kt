package com.Frank.automarket.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.Frank.automarket.MainActivity
import data.session.SessionManager
import com.Frank.automarket.databinding.ActivityLoginBinding
import controller.LoginViewModel
import controller.LoginUiState
import util.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.fetchUserId() != -1) {
            navigateToMainApp()
            return
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when (it) {
                    is LoginUiState.Loading -> {
                        binding.progressBar.visible()
                    }
                    is LoginUiState.Success -> {
                        binding.progressBar.gone()
                        sessionManager.saveUserId(it.user.id)
                        toast("¡Inicio de sesión exitoso!")
                        navigateToMainApp()
                    }
                    is LoginUiState.Error -> {
                        binding.progressBar.gone()
                        toast("Error: ${it.message}")
                    }
                    is LoginUiState.Idle -> {
                        binding.progressBar.gone()
                    }
                }
            }
        }
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}