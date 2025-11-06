package adapter

import Entity.Vehiculo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Frank.automarket.R
import com.Frank.automarket.databinding.ItemVehiculoBinding
import com.bumptech.glide.Glide
import java.io.File

class VehiculoAdapter(
    private val onVehiculoClick: (Vehiculo) -> Unit
) : ListAdapter<Vehiculo, VehiculoAdapter.VehiculoViewHolder>(VehiculoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehiculoViewHolder {
        val binding = ItemVehiculoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehiculoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehiculoViewHolder, position: Int) {
        val vehiculo = getItem(position)
        holder.bind(vehiculo)
        holder.itemView.setOnClickListener { onVehiculoClick(vehiculo) }
    }

    class VehiculoViewHolder(private val binding: ItemVehiculoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehiculo: Vehiculo) {
            binding.apply {
                tvTitulo.text = vehiculo.getTituloCompleto()
                tvSubtitulo.text = vehiculo.getSubtitulo()
                tvPrecio.text = vehiculo.getPrecioFormateado()
                tvKilometraje.text = vehiculo.getKilometrajeFormateado()
                tvTransmision.text = vehiculo.transmision
                tvBadge.text = vehiculo.estado
                
                tvBadge.setBackgroundColor(
                    itemView.context.getColor(if (vehiculo.esNuevo()) R.color.badge_nuevo else R.color.badge_usado)
                )

                if (vehiculo.tieneImagen()) {
                    Glide.with(itemView.context)
                        .load(File(vehiculo.imagenUri!!))
                        .centerCrop()
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .into(ivVehiculo)
                } else {
                    ivVehiculo.setImageResource(R.drawable.ic_car_placeholder)
                }
            }
        }
    }

    class VehiculoDiffCallback : DiffUtil.ItemCallback<Vehiculo>() {
        override fun areItemsTheSame(oldItem: Vehiculo, newItem: Vehiculo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Vehiculo, newItem: Vehiculo): Boolean = oldItem == newItem
    }
}