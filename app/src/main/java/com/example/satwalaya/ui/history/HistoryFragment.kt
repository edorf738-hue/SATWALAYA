package com.example.satwalaya.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.navigation.fragment.findNavController

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        loadBookings()
    }

    private fun loadBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (_binding == null) return@addOnSuccessListener

                if (result.isEmpty) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvHistory.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvHistory.visibility = View.VISIBLE

                    val bookings = result.documents.map { doc ->
                        mapOf(
                            "id" to doc.id,
                            "serviceName" to (doc.getString("serviceName") ?: ""),
                            "petNames" to (doc.getString("petNames") ?: ""),
                            "checkIn" to (doc.getString("checkIn") ?: ""),
                            "checkOut" to (doc.getString("checkOut") ?: "-"),
                            "totalPrice" to (doc.getLong("totalPrice")?.toInt() ?: 0),
                            "status" to (doc.getString("status") ?: ""),
                            "paymentMethod" to (doc.getString("paymentMethod") ?: ""),
                            "ownerName" to (doc.getString("ownerName") ?: ""),
                            "ownerPhone" to (doc.getString("ownerPhone") ?: "")
                        )
                    }

                    binding.rvHistory.adapter = HistoryAdapter(bookings,
                        onItemClick = { booking -> showDetail(booking) },
                        onCancelClick = { booking -> showCancelDialog(booking) }
                    )
                }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDetail(booking: Map<String, Any>) {
        val status = booking["status"] as String

        // Kalau booking aktif (sudah confirmed/check-in), buka Daily Updates
        if (status == "Confirmed" || status == "Check-in") {
            val bundle = Bundle().apply {
                putString("bookingId", booking["id"] as String)
                putString("petNames", booking["petNames"] as String)
            }
            findNavController().navigate(R.id.action_history_to_dailyUpdates, bundle)
            return
        }

        // Kalau status lain, tampilkan detail biasa
        val checkOut = booking["checkOut"] as String
        val dates = if (checkOut == "-") {
            booking["checkIn"] as String
        } else {
            "${booking["checkIn"]} - $checkOut"
        }

        val method = when (booking["paymentMethod"] as String) {
            "cod" -> "COD (Bayar di tempat)"
            "transfer" -> "Transfer Bank"
            else -> booking["paymentMethod"] as String
        }

        AlertDialog.Builder(requireContext())
            .setTitle(booking["serviceName"] as String)
            .setMessage(
                "Hewan: ${booking["petNames"]}\n" +
                        "Tanggal: $dates\n" +
                        "Total: ${formatPrice(booking["totalPrice"] as Int)}\n" +
                        "Pembayaran: $method\n" +
                        "Status: $status\n\n" +
                        "Pemilik: ${booking["ownerName"]}\n" +
                        "WhatsApp: ${booking["ownerPhone"]}"
            )
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun showCancelDialog(booking: Map<String, Any>) {
        val status = booking["status"] as String

        if (status == "Selesai") {
            showReviewDialog(booking)
            return
        }

        val id = booking["id"] as String
        val name = booking["petNames"] as String

        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Booking")
            .setMessage("Yakin ingin membatalkan booking untuk $name?")
            .setPositiveButton("Ya, Batalkan") { _, _ ->
                db.collection("bookings").document(id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Booking dibatalkan", Toast.LENGTH_SHORT).show()
                        loadBookings()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal membatalkan", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showReviewDialog(booking: Map<String, Any>) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_review, null)

        val ratingBar = dialogView.findViewById<android.widget.RatingBar>(R.id.ratingBar)
        val etReview = dialogView.findViewById<android.widget.EditText>(R.id.etReview)

        AlertDialog.Builder(requireContext())
            .setTitle("Review untuk ${booking["serviceName"]}")
            .setView(dialogView)
            .setPositiveButton("Kirim") { _, _ ->
                val rating = ratingBar.rating
                val reviewText = etReview.text.toString().trim()

                if (rating == 0f) {
                    Toast.makeText(requireContext(), "Pilih rating dulu", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val userName = booking["ownerName"] as? String ?: ""

                val review = hashMapOf(
                    "userId" to userId,
                    "userName" to userName,
                    "bookingId" to (booking["id"] as String),
                    "serviceName" to (booking["serviceName"] as String),
                    "petNames" to (booking["petNames"] as String),
                    "rating" to rating,
                    "reviewText" to reviewText,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )

                db.collection("reviews").add(review)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Review berhasil dikirim!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal mengirim review", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}