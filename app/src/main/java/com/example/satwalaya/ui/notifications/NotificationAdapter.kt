package com.example.satwalaya.ui.notifications

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val items: MutableList<NotificationItem>,
    private val onClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvNotifTitle.text = item.title
            tvNotifMessage.text = item.message
            tvNotifIcon.text = when (item.type) {
                "booking_confirmed" -> "✅"
                "booking_cancelled" -> "❌"
                "daily_update" -> "📋"
                else -> "🔔"
            }

            viewUnreadDot.visibility = if (!item.isRead) View.VISIBLE else View.GONE

            item.createdAt?.toDate()?.let { date ->
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
                tvNotifTime.text = sdf.format(date)
            }

            root.setBackgroundColor(
                if (!item.isRead) Color.parseColor("#F3F0FF") else Color.WHITE
            )

            root.setOnClickListener {
                // Kirim item asli (dengan id) ke onClick, bukan copy
                onClick(item)
                // Update UI lokal langsung
                if (!item.isRead) {
                    items[position] = item.copy(isRead = true)
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}