package com.example.satwalaya.data.model

data class Pet(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "",       // Kucing, Anjing, Kelinci
    val breed: String = "",
    val age: String = "",
    val weight: String = "",
    val allergy: String = "",
    val feedSchedule: String = "",
    val feedType: String = ""
)