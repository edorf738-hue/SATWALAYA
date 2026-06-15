package com.example.satwalaya.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class BookingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun saveBooking(
        userId: String, serviceName: String, ownerName: String,
        ownerPhone: String, petType: String, petName: String,
        petAge: String, petAllergy: String, bookingDate: String,
        totalPrice: Int, paymentMethod: String,
        onSuccess: (bookingId: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val booking = hashMapOf(
            "userId" to userId, "serviceName" to serviceName,
            "ownerName" to ownerName, "ownerPhone" to ownerPhone,
            "petType" to petType, "petName" to petName,
            "petAge" to petAge, "petAllergy" to petAllergy,
            "bookingDate" to bookingDate, "totalPrice" to totalPrice,
            "paymentMethod" to paymentMethod, "paymentStatus" to "pending",
            "paymentProof" to "", "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("bookings").add(booking)
            .addOnSuccessListener { doc -> onSuccess(doc.id) }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun uploadPaymentProof(
        bookingId: String, imageUri: Uri,
        onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("payment_proof/$bookingId.jpg")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    db.collection("bookings").document(bookingId)
                        .update("paymentProof", url.toString(), "paymentStatus", "waiting_confirmation")
                        .addOnSuccessListener { onSuccess() }
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getBookingHistory(
        userId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.data?.plus("bookingId" to doc.id)
                }
                onSuccess(list)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun deleteBooking( // ← tambahan
        bookingId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("bookings").document(bookingId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun countActiveBookings(userId: String, onResult: (Int) -> Unit): ListenerRegistration {
        return db.collection("bookings")
            .whereEqualTo("userId", userId)
            .whereIn("status", listOf("Menunggu Kedatangan", "Menunggu Verifikasi", "Confirmed", "Check-in"))
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) { onResult(0); return@addSnapshotListener }
                onResult(snapshots.size())
            }
    }
}