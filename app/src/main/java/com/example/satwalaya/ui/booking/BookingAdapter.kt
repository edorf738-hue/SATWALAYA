package com.example.satwalaya.ui.booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.data.model.Booking
import com.example.satwalaya.R
import com.example.satwalaya.databinding.ItemBookingHistoryBinding

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onCancelClick: (Booking) -> Unit,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemBookingHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        val binding = holder.binding
        val context = binding.root.context

        binding.tvHistoryIcon.text = booking.getServiceEmoji()
        binding.tvHistoryServiceName.text = booking.serviceType
        binding.tvHistoryPetName.text = booking.petName
        binding.tvHistoryDate.text = "📅 ${booking.bookingDate}"
        binding.tvHistoryPrice.text = booking.getFormattedPrice()

        when (booking.status) {
            "completed" -> {
                binding.tvHistoryStatus.text = "Selesai"
                binding.tvHistoryStatus.setTextColor(ContextCompat.getColor(context, R.color.status_green))
                binding.tvHistoryStatus.setBackgroundResource(R.drawable.bg_status_completed)
                binding.btnCancelHistory.visibility = View.GONE
            }
            else -> {
                binding.tvHistoryStatus.text = "Tagihan Aktif"
                binding.tvHistoryStatus.setTextColor(ContextCompat.getColor(context, R.color.text_amber))
                binding.tvHistoryStatus.setBackgroundResource(R.drawable.bg_status_pending)
                binding.btnCancelHistory.visibility = View.VISIBLE
            }
        }

        binding.btnCancelHistory.text = context.getString(R.string.cancel_booking)
        binding.btnCancelHistory.setOnClickListener { onCancelClick(booking) }
        binding.root.setOnClickListener { onItemClick(booking) }

        binding.tvHistoryIcon.setBackgroundResource(when {
            booking.serviceType.contains("Hotel") -> R.drawable.bg_service_icon_orange
            booking.serviceType.contains("Grooming") -> R.drawable.bg_service_icon_purple
            booking.serviceType.contains("Pickup") -> R.drawable.bg_service_icon_blue
            else -> R.drawable.bg_service_icon_teal
        })
    }

    override fun getItemCount(): Int = bookings.size

    fun updateData(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    fun getBookingList(): List<Booking> = bookings // ← tambahan
}