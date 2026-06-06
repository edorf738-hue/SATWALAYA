package com.example.satwalaya.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.R

class HistoryAdapter(
    private val bookings: List<Map<String, Any>>,
    private val onItemClick: (Map<String, Any>) -> Unit,
    private val onCancelClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvPetNames: TextView = view.findViewById(R.id.tvPetNames)
        val tvDates: TextView = view.findViewById(R.id.tvDates)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val btnCancel: TextView = view.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]

        holder.tvServiceName.text = booking["serviceName"] as String
        holder.tvPetNames.text = "🐾 ${booking["petNames"]}"

        val totalPrice = booking["totalPrice"] as Int
        holder.tvPrice.text = "Rp${String.format("%,d", totalPrice).replace(',', '.')}"

        // Dates
        val checkOut = booking["checkOut"] as String
        holder.tvDates.text = if (checkOut == "-") {
            "📅 ${booking["checkIn"]}"
        } else {
            "📅 ${booking["checkIn"]} - $checkOut"
        }

        // Status badge
        val status = booking["status"] as String
        holder.tvStatus.text = status
        when (status) {
            "Menunggu Kedatangan", "Menunggu Verifikasi" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_icon_amber)
                holder.tvStatus.setTextColor(holder.itemView.resources.getColor(R.color.amber_dark, null))
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnCancel.text = "❌ Batalkan booking"
                holder.btnCancel.setTextColor(holder.itemView.resources.getColor(R.color.text_secondary, null))
                holder.btnCancel.setBackgroundResource(R.drawable.bg_input_field)
                holder.btnCancel.setOnClickListener { onCancelClick(booking) }
            }
            "Confirmed", "Check-in" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_icon_green)
                holder.tvStatus.setTextColor(holder.itemView.resources.getColor(R.color.green_dark, null))
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnCancel.text = "📷 Lihat daily updates"
                holder.btnCancel.setTextColor(holder.itemView.resources.getColor(R.color.purple_primary, null))
                holder.btnCancel.setBackgroundResource(R.drawable.bg_input_field)
                holder.btnCancel.setOnClickListener { onItemClick(booking) }
            }
            "Selesai" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_icon_green)
                holder.tvStatus.setTextColor(holder.itemView.resources.getColor(R.color.green_dark, null))
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnCancel.text = "⭐ Kasih review"
                holder.btnCancel.setTextColor(holder.itemView.resources.getColor(R.color.amber_dark, null))
                holder.btnCancel.setBackgroundResource(R.drawable.bg_icon_amber)
                holder.btnCancel.setOnClickListener { onCancelClick(booking) }
            }
            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_input_field)
                holder.tvStatus.setTextColor(holder.itemView.resources.getColor(R.color.text_secondary, null))
                holder.btnCancel.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = bookings.size
}