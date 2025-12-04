package com.Frank.automarket.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.Frank.automarket.data.network.ApiClient
import com.Frank.automarket.data.network.model.VehiculoDto
import com.Frank.automarket.databinding.ItemVehiculoBinding
import com.Frank.automarket.R
import util.FormatUtils
import util.visible
import util.gone

class VehiculoAdapter(
    private val onItemClicked: (VehiculoDto) -> Unit
) : ListAdapter<VehiculoDto, VehiculoAdapter.VehiculoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehiculoViewHolder {
        val binding = ItemVehiculoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehiculoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehiculoViewHolder, position: Int) {
        val vehiculo = getItem(position)
        holder.bind(vehiculo)
        holder.itemView.setOnClickListener { onItemClicked(vehiculo) }
    }

    inner class VehiculoViewHolder(private val binding: ItemVehiculoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehiculo: VehiculoDto) {
            binding.apply {
                tvTitulo.text = "${vehiculo.marca} ${vehiculo.modelo}"
                tvSubtitulo.text = "${vehiculo.anio} • ${vehiculo.tipo}"
                tvPrecio.text = FormatUtils.formatPrice(vehiculo.precio)
                tvKilometraje.text = FormatUtils.formatKilometraje(vehiculo.kilometraje)
                tvTransmision.text = vehiculo.transmision

                if (vehiculo.imagenUrl != null) {
                    val fullImageUrl = ApiClient.BASE_URL.removeSuffix("/") + vehiculo.imagenUrl
                    Glide.with(itemView.context)
                        .load(fullImageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_car_placeholder)
                        .into(ivVehiculo)
                } else {
                    ivVehiculo.setImageResource(R.drawable.ic_car_placeholder)
                }

                if (vehiculo.estado.isNotBlank()) {
                    tvBadge.visible()
                    tvBadge.text = vehiculo.estado
                } else {
                    tvBadge.gone()
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<VehiculoDto>() {
        override fun areItemsTheSame(oldItem: VehiculoDto, newItem: VehiculoDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VehiculoDto, newItem: VehiculoDto): Boolean {
            return oldItem == newItem
        }
    }
}