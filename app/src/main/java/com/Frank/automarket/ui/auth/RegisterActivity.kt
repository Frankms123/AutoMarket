package com.Frank.automarket.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.Frank.automarket.MainActivity
import data.session.SessionManager
import com.Frank.automarket.databinding.ActivityRegisterBinding
import controller.RegisterViewModel
import controller.RegisterUiState
import util.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

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

        binding.btnGoToLogin.setOnClickListener {
            finish()
        }
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
                        toast(getString(com.Frank.automarket.R.string.msg_registration_successful))
                        finish()
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
}