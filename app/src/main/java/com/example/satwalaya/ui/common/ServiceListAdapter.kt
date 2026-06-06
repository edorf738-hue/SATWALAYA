package com.example.satwalaya.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.data.model.ServiceItem
import com.example.satwalaya.databinding.ItemServiceBinding

class ServiceListAdapter(
    private val services: List<ServiceItem>,
    private val onBookClick: (ServiceItem) -> Unit
) : RecyclerView.Adapter<ServiceListAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        with(holder.binding) {
            tvServiceName.text = service.name
            tvServiceDesc.text = service.description
            tvServiceIcon.text = service.icon
            tvServiceIcon.setBackgroundResource(service.imageResId)
            tvServicePrice.text = service.price
            tvServiceIcon.text = when {
                service.name.contains("Grooming") -> "✂️"
                service.name.contains("Kucing") -> "🐱"
                service.name.contains("Anjing") -> "🐶"
                service.name.contains("Kelinci") -> "🐰"
                else -> "🏨"
            }

            btnBook.setOnClickListener { onBookClick(service) }
        }
    }

    override fun getItemCount(): Int = services.size
}