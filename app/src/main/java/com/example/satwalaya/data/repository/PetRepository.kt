package com.example.satwalaya.data.repository

import com.example.satwalaya.data.model.Pet
import com.google.firebase.firestore.FirebaseFirestore

class PetRepository {

    private val db = FirebaseFirestore.getInstance()

    fun savePet(
        pet: Pet,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "userId" to pet.userId,
            "name" to pet.name,
            "type" to pet.type,
            "breed" to pet.breed,
            "age" to pet.age,
            "weight" to pet.weight,
            "allergy" to pet.allergy,
            "feedSchedule" to pet.feedSchedule,
            "feedType" to pet.feedType
        )

        if (pet.id.isEmpty()) {
            db.collection("pets").add(data)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        } else {
            db.collection("pets").document(pet.id).set(data)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun getPets(
        userId: String,
        onSuccess: (List<Pet>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("pets")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val pets = result.documents.map { doc ->
                    Pet(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        name = doc.getString("name") ?: "",
                        type = doc.getString("type") ?: "",
                        breed = doc.getString("breed") ?: "",
                        age = doc.getString("age") ?: "",
                        weight = doc.getString("weight") ?: "",
                        allergy = doc.getString("allergy") ?: "",
                        feedSchedule = doc.getString("feedSchedule") ?: "",
                        feedType = doc.getString("feedType") ?: ""
                    )
                }
                onSuccess(pets)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun deletePet(
        petId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("pets").document(petId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}