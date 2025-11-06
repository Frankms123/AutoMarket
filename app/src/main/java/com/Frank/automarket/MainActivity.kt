package com.Frank.automarket

import adapter.VehiculoAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.Frank.automarket.databinding.ActivityMainBinding
import Entity.Vehiculo
import controller.VehiculoController
import util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: VehiculoController
    private lateinit var adapter: VehiculoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupController()
        setupRecyclerView()
        setupListeners()
        observeVehiculos()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.titulo_lista)
    }

    private fun setupController() {
        controller = VehiculoController(this)
    }

    private fun setupRecyclerView() {
        adapter = VehiculoAdapter { vehiculo: Vehiculo -> // Corregido
            val intent = Intent(this, DetalleVehiculoActivity::class.java).apply {
                putExtra(EXTRA_VEHICULO_ID, vehiculo.id)
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

    private fun observeVehiculos() {
        lifecycleScope.launch {
            controller.obtenerTodosLosVehiculos().collectLatest { vehiculos ->
                if (vehiculos.isEmpty()) {
                    mostrarEstadoVacio()
                } else {
                    mostrarLista(vehiculos)
                }
            }
        }
    }

    private fun mostrarEstadoVacio() {
        binding.apply {
            rvVehiculos.gone()
            layoutEmpty.root.visible()
            layoutEmpty.tvEmptyTitle.text = getString(R.string.empty_state_titulo)
            layoutEmpty.tvEmptyMessage.text = getString(R.string.empty_state_mensaje)
        }
    }

    private fun mostrarLista(vehiculos: List<Vehiculo>) {
        binding.apply {
            layoutEmpty.root.gone()
            rvVehiculos.visible()
            adapter.submitList(vehiculos)
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
                    buscarVehiculos(newText.orEmpty())
                    return true
                }
            })
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> { mostrarDialogoFiltros(); true }
            R.id.action_sort -> { mostrarDialogoOrdenamiento(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun buscarVehiculos(query: String) {
        lifecycleScope.launch {
            controller.buscarVehiculos(query).collectLatest { vehiculos ->
                if (vehiculos.isEmpty() && query.isNotEmpty()) {
                    mostrarEstadoBusquedaVacia()
                } else {
                    mostrarLista(vehiculos)
                }
            }
        }
    }

    private fun mostrarEstadoBusquedaVacia() {
        binding.apply {
            rvVehiculos.gone()
            layoutEmpty.root.visible()
            layoutEmpty.tvEmptyTitle.text = getString(R.string.empty_state_busqueda)
            layoutEmpty.tvEmptyMessage.text = getString(R.string.empty_state_busqueda_mensaje)
        }
    }

    private fun mostrarDialogoFiltros() {
        // Lógica de filtros aquí
    }

    private fun mostrarDialogoOrdenamiento() {
        // Lógica de ordenamiento aquí
    }

    override fun onResume() {
        super.onResume()
        observeVehiculos()
    }

    companion object {
        const val EXTRA_VEHICULO_ID = "vehiculo_id"
    }
}