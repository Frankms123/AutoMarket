package com.Frank.automarket

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.Frank.automarket.data.session.SessionManager
import com.Frank.automarket.databinding.ActivityMainBinding
import com.Frank.automarket.ui.auth.LoginActivity
import com.Frank.automarket.ui.main.MainUiState
import com.Frank.automarket.ui.main.MainViewModel
import com.Frank.automarket.ui.main.VehiculoAdapter
import util.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: VehiculoAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.cargarVehiculos()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.titulo_lista)
    }

    private fun setupRecyclerView() {
        adapter = VehiculoAdapter { vehiculo ->
            val intent = Intent(this, DetalleVehiculoActivity::class.java).apply {
                putExtra(DetalleVehiculoActivity.EXTRA_VEHICULO_ID, vehiculo.id)
            }
            startActivity(intent)
        }

        binding.rvVehiculos.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.fabAgregar.setOnClickListener {
            val intent = Intent(this, FormularioVehiculoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is MainUiState.Loading -> {
                        binding.progressBar.visible()
                        binding.rvVehiculos.gone()
                        binding.layoutEmpty.root.gone()
                    }
                    is MainUiState.Success -> {
                        binding.progressBar.gone()
                        if (state.vehiculos.isEmpty()) {
                            mostrarEstadoVacio(getString(R.string.empty_state_titulo), getString(R.string.empty_state_mensaje))
                        } else {
                            mostrarLista()
                            adapter.submitList(state.vehiculos)
                        }
                    }
                    is MainUiState.Error -> {
                        binding.progressBar.gone()
                        mostrarEstadoVacio("Error", state.message)
                    }
                }
            }
        }
    }

    private fun mostrarEstadoVacio(titulo: String, mensaje: String) {
        binding.apply {
            rvVehiculos.gone()
            layoutEmpty.root.visible()
            layoutEmpty.tvEmptyTitle.text = titulo
            layoutEmpty.tvEmptyMessage.text = mensaje
        }
    }

    private fun mostrarLista() {
        binding.apply {
            layoutEmpty.root.gone()
            rvVehiculos.visible()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            queryHint = getString(R.string.hint_buscar)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.buscarVehiculos(newText.orEmpty())
                    return true
                }
            })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> { 
                logout()
                true 
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarVehiculos()
    }
}