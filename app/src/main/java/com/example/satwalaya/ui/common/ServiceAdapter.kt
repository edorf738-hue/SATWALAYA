package com.example.satwalaya.ui.common

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.R
import com.example.satwalaya.data.model.ServiceItem
import com.example.satwalaya.databinding.ItemServiceCardBinding

class ServiceAdapter(
    private val services: List<ServiceItem>,
    private val onItemClick: (ServiceItem) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(val binding: ItemServiceCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        with(holder.binding) {
            tvServiceName.text = service.name
            tvServiceDescription.text = service.description
            tvServicePrice.text = service.price
            tvServiceRating.text = "⭐ ${service.rating} (${service.reviewCount})"

            if (service.originalPrice.isNotEmpty()) {
                tvOriginalPrice.visibility = View.VISIBLE
                tvOriginalPrice.text = service.originalPrice
                tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvOriginalPrice.visibility = View.GONE
            }

            if (service.badge.isNotEmpty()) {
                tvServiceBadge.visibility = View.VISIBLE
                tvServiceBadge.text = service.badge
                // Update badge colors based on text
                when (service.badge) {
                    "Populer" -> tvServiceBadge.setBackgroundResource(R.drawable.bg_badge_populer)
                    "Terfavorit" -> tvServiceBadge.setBackgroundResource(R.drawable.bg_badge_favorit)
                    else -> tvServiceBadge.setBackgroundResource(R.drawable.bg_status_completed)
                }
            } else {
                tvServiceBadge.visibility = View.GONE
            }

            // Bind real image
            if (service.imageResId != 0) {
                ivServiceImage.setImageResource(service.imageResId)
                ivServiceImage.visibility = View.VISIBLE
            } else {
                ivServiceImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            root.setOnClickListener { onItemClick(service) }
        }
    }

    override fun getItemCount(): Int = services.size
}