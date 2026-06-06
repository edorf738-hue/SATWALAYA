package com.example.satwalaya.data.model

data class ServiceItem(
    val id: String,
    val name: String,
    val description: String,
    val imageResId: Int,
    val price: String,
    val icon: String = "",      // ← tambah ini
    val originalPrice: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val badge: String = "",
    val iconColor: Int
)