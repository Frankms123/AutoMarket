package com.Frank.automarket.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.Frank.automarket.MainActivity
import com.Frank.automarket.data.session.SessionManager
import com.Frank.automarket.databinding.ActivityRegisterBinding
import util.toast
import util.gone
import util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        if (sessionManager.fetchUserId() != -1) {
            navigateToMainApp()
            return
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            viewModel.register(email, password, confirmPassword)
        }

        binding.btnGoToLogin.gone()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when (it) {
                    is RegisterUiState.Loading -> {
                        binding.progressBar.visible()
                        binding.btnRegister.isEnabled = false
                    }
                    is RegisterUiState.Success -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                        sessionManager.saveUserId(it.user.id)
                        toast(getString(com.Frank.automarket.R.string.msg_registro_exitoso))
                        navigateToMainApp()
                    }
                    is RegisterUiState.Error -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                        toast("Error: ${it.message}")
                    }
                    is RegisterUiState.Idle -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
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