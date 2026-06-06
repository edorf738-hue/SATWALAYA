package com.example.satwalaya.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.data.repository.BookingRepository
import com.example.satwalaya.R
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: BookingRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = BookingRepository()
        sessionManager = SessionManager(requireContext())

        val ownerName    = arguments?.getString("ownerName") ?: ""
        val address      = arguments?.getString("address") ?: ""
        val petName      = arguments?.getString("petName") ?: ""
        val petType = arguments?.getString("petType") ?: ""
        val petAge       = arguments?.getString("petAge") ?: ""
        val petAllergies = arguments?.getString("petAllergies") ?: ""
        val serviceName  = arguments?.getString("serviceName") ?: ""
        val servicePrice = arguments?.getInt("servicePrice") ?: 0
        val checkIn      = arguments?.getLong("checkIn") ?: -1L
        val checkOut     = arguments?.getLong("checkOut") ?: -1L
        val nights       = arguments?.getLong("nights") ?: 0L
        val totalHarga   = arguments?.getLong("totalHarga") ?: servicePrice.toLong()

        binding.tvSummaryService.text = serviceName
        binding.tvSummaryPrice.text = "Rp ${String.format("%,d", totalHarga).replace(',', '.')}"

        binding.btnPayNow.setOnClickListener {
            val selectedId = binding.rgPayment.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Silakan pilih metode pembayaran", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val metodeBayar = when (selectedId) {
                R.id.rbQRIS -> "QRIS"
                R.id.rbCOD  -> "COD"
                else        -> ""
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val date = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
            binding.btnPayNow.isEnabled = false

            repository.saveBooking(
                userId        = userId,
                serviceName   = serviceName,
                ownerName     = ownerName,
                ownerPhone    = address,
                petType       = petType,
                petName       = petName,
                petAge        = petAge,
                petAllergy    = petAllergies,
                bookingDate   = date,
                totalPrice    = totalHarga.toInt(),
                paymentMethod = metodeBayar,
                onSuccess = { bookingId ->
                    if (_binding == null) return@saveBooking

                    val pointsEarned = totalHarga.toInt() / 1000
                    sessionManager.addPoints(pointsEarned)

                    val bundle = Bundle().apply {
                        putString("bookingId",   bookingId)
                        putString("ownerName",   ownerName)
                        putString("serviceName", serviceName)
                        putInt("servicePrice",   servicePrice)
                        putString("metodeBayar", metodeBayar)
                        putLong("totalHarga",    totalHarga)
                    }

                    findNavController().navigate(
                        R.id.action_paymentFragment_to_orderSuccessFragment, bundle
                    )
                },
                onFailure = { e ->
                    if (_binding == null) return@saveBooking

                    binding.btnPayNow.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}