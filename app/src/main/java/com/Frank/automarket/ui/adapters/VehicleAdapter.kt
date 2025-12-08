package com.Frank.automarket.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.Frank.automarket.R
import com.Frank.automarket.databinding.ItemVehicleBinding
import data.network.ApiClient
import entity.VehicleDto
import util.formatAsMileage
import util.formatAsPrice

class VehicleAdapter(
    private val onItemClicked: (VehicleDto) -> Unit
) : ListAdapter<VehicleDto, VehicleAdapter.VehicleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = getItem(position)
        holder.bind(vehicle)
        holder.itemView.setOnClickListener { onItemClicked(vehicle) }
    }

    inner class VehicleViewHolder(private val binding: ItemVehicleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicle: VehicleDto) {
            binding.apply {
                tvTitle.text = "${vehicle.brand} ${vehicle.model}"
                tvSubtitle.text = "${vehicle.year} • ${vehicle.type}"
                tvPrice.text = vehicle.price.formatAsPrice()
                tvMileage.text = vehicle.mileage.formatAsMileage()
                tvTransmission.text = vehicle.transmission
                tvBadge.text = vehicle.condition

                if (vehicle.imageUrl != null) {
                    val fullImageUrl = ApiClient.BASE_URL.removeSuffix("/") + vehicle.imageUrl
                    Glide.with(itemView.context)
                        .load(fullImageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_car_placeholder)
                        .into(ivVehicle)
                } else {
                    ivVehicle.setImageResource(R.drawable.ic_car_placeholder)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<VehicleDto>() {
        override fun areItemsTheSame(oldItem: VehicleDto, newItem: VehicleDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VehicleDto, newItem: VehicleDto): Boolean {
            return oldItem == newItem
        }
    }
}