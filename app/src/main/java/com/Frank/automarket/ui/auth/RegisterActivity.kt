package com.Frank.automarket.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.Frank.automarket.databinding.ActivityRegisterBinding
import controller.RegisterViewModel
import controller.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import util.gone
import util.toast
import util.visible

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.btnGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when (it) {
                    is UiState.Loading -> {
                        binding.progressBar.visible()
                        binding.btnRegister.isEnabled = false
                    }
                    is UiState.Success -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                        toast(getString(com.Frank.automarket.R.string.msg_registration_successful))
                        finish()
                    }
                    is UiState.Error -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                        toast("Error: ${it.message}")
                    }
                    is UiState.Idle -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                    }
                    else -> {
                        binding.progressBar.gone()
                        binding.btnRegister.isEnabled = true
                    }
                }
            }
        }
    }
}
