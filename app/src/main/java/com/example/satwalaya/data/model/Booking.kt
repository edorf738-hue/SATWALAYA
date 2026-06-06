package com.example.satwalaya.data.model

data class Booking(
    val id: Int = 0,
    val ownerName: String,
    val address: String,
    val petName: String,
    val petAge: String,
    val petAllergies: String,
    val serviceType: String,
    val bookingDate: String,
    val price: Int,
    var status: String = "Pending",
    val metodeBayar: String = "COD",
    val paymentProof: String = ""
) {
    fun getFormattedPrice(): String {
        return if (price <= 0) "Gratis" else "Rp ${String.format("%,d", price).replace(',', '.')}"
    }

    fun getServiceEmoji(): String {
        return when {
            serviceType.contains("Hotel", true) -> "🏨"
            serviceType.contains("Grooming", true) -> "✂️"
            serviceType.contains("Pickup", true) -> "🚗"
            else -> "🐾"
        }
    }
}