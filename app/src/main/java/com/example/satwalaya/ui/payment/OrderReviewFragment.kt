package com.example.satwalaya.ui.payment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.R
import com.example.satwalaya.data.repository.BookingRepository
import com.example.satwalaya.databinding.FragmentOrderReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class OrderReviewFragment : Fragment() {
    private var _binding: FragmentOrderReviewBinding? = null
    private val binding get() = _binding!!

    private var selectedMethod = "" // "cod" atau "transfer"
    private var proofUri: Uri? = null

    // Data dari BookingFormFragment
    private var serviceName = ""
    private var ownerName = ""
    private var ownerPhone = ""
    private var petNames = ""
    private var checkIn = ""
    private var checkOut = ""
    private var totalPrice = 0
    private var addGrooming = false
    private var groomingType = ""

    // Image picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            proofUri = it
            binding.tvUploadStatus.text = "✅ Bukti berhasil dipilih"
            binding.tvUploadStatus.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Parse data
        arguments?.let {
            serviceName = it.getString("serviceName", "")
            ownerName = it.getString("ownerName", "")
            ownerPhone = it.getString("ownerPhone", "")
            petNames = it.getString("petNames", "")
            checkIn = it.getString("checkIn", "")
            checkOut = it.getString("checkOut", "-")
            totalPrice = it.getInt("totalPrice", 0)
            addGrooming = it.getBoolean("addGrooming", false)
            groomingType = it.getString("groomingType", "")
        }

        // Display summary
        binding.tvServiceName.text = serviceName
        binding.tvPetNames.text = petNames
        binding.tvCheckIn.text = checkIn
        binding.tvContact.text = "$ownerName • $ownerPhone"
        binding.tvTotalPrice.text = formatPrice(totalPrice)

        if (checkOut == "-") {
            binding.checkOutRow.visibility = View.GONE
            binding.tvDateLabel.text = "Tanggal"
        } else {
            binding.checkOutRow.visibility = View.VISIBLE
            binding.tvCheckOut.text = checkOut
        }

        // Grooming info
        if (addGrooming) {
            val groomLabel = if (groomingType == "fresh") "Paket Fresh" else "Full Grooming"
            binding.tvServiceName.text = "$serviceName + $groomLabel"
        }

        // Back button
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Payment method selection
        binding.cardCod.setOnClickListener { selectMethod("cod") }
        binding.cardTransfer.setOnClickListener { selectMethod("transfer") }

        // Upload bukti
        binding.btnUploadProof.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Pay button
        binding.btnPay.setOnClickListener { processPayment() }
    }

    private fun selectMethod(method: String) {
        selectedMethod = method

        if (method == "cod") {
            binding.cardCod.setBackgroundResource(R.drawable.bg_icon_green)
            binding.cardTransfer.setBackgroundColor(resources.getColor(R.color.white, null))
            binding.transferInfo.visibility = View.GONE
            binding.btnPay.text = "Konfirmasi Booking (COD)"
        } else {
            binding.cardTransfer.setBackgroundResource(R.drawable.bg_pet_icon)
            binding.cardCod.setBackgroundColor(resources.getColor(R.color.white, null))
            binding.transferInfo.visibility = View.VISIBLE
            binding.btnPay.text = "Konfirmasi & Upload Bukti"
        }
    }

    private fun processPayment() {
        if (selectedMethod.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedMethod == "transfer" && proofUri == null) {
            Toast.makeText(requireContext(), "Upload bukti transfer dulu", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPay.isEnabled = false
        binding.btnPay.text = "Memproses..."

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val db = FirebaseFirestore.getInstance()

        val booking = hashMapOf(
            "userId" to userId,
            "serviceName" to serviceName,
            "ownerName" to ownerName,
            "ownerPhone" to ownerPhone,
            "petNames" to petNames,
            "checkIn" to checkIn,
            "checkOut" to checkOut,
            "totalPrice" to totalPrice,
            "paymentMethod" to selectedMethod,
            "paymentStatus" to if (selectedMethod == "cod") "pending" else "waiting_verification",
            "paymentProof" to "",
            "status" to if (selectedMethod == "cod") "Menunggu Kedatangan" else "Menunggu Verifikasi",
            "addGrooming" to addGrooming,
            "groomingType" to groomingType,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("bookings").add(booking)
            .addOnSuccessListener { doc ->
                if (selectedMethod == "transfer" && proofUri != null) {
                    uploadProof(doc.id)
                } else {
                    navigateToSuccess()
                }
            }
            .addOnFailureListener {
                binding.btnPay.isEnabled = true
                binding.btnPay.text = "Bayar sekarang"
                Toast.makeText(requireContext(), "Gagal memproses booking", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProof(bookingId: String) {
        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child("payment_proof/$bookingId.jpg")

        ref.putFile(proofUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    FirebaseFirestore.getInstance()
                        .collection("bookings").document(bookingId)
                        .update("paymentProof", url.toString())
                        .addOnSuccessListener { navigateToSuccess() }
                }
            }
            .addOnFailureListener {
                // Booking udah tersimpan, bukti gagal upload tapi gpp
                navigateToSuccess()
            }
    }

    private fun navigateToSuccess() {
        val bundle = Bundle().apply {
            putString("serviceName", serviceName)
            putString("petNames", petNames)
            putString("checkIn", checkIn)
            putString("checkOut", checkOut)
            putInt("totalPrice", totalPrice)
            putString("paymentMethod", selectedMethod)
        }
        findNavController().navigate(
            R.id.action_orderReviewFragment_to_orderSuccessFragment,
            bundle
        )
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}