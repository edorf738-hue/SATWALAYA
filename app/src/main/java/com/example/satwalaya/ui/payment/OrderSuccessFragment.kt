package com.example.satwalaya.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentOrderSuccessBinding

class OrderSuccessFragment : Fragment() {
    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val serviceName = it.getString("serviceName", "")
            val petNames = it.getString("petNames", "")
            val checkIn = it.getString("checkIn", "")
            val checkOut = it.getString("checkOut", "-")
            val totalPrice = it.getInt("totalPrice", 0)
            val paymentMethod = it.getString("paymentMethod", "")

            binding.tvServiceName.text = serviceName
            binding.tvPetNames.text = petNames
            binding.tvTotal.text = formatPrice(totalPrice)

            // Dates
            if (checkOut == "-") {
                binding.tvDates.text = checkIn
            } else {
                binding.tvDates.text = "$checkIn - $checkOut"
            }

            // Payment method
            binding.tvPaymentMethod.text = when (paymentMethod) {
                "cod" -> "COD (Bayar di tempat)"
                "transfer" -> "Transfer Bank"
                else -> paymentMethod
            }

            // Subtitle & info based on payment method
            if (paymentMethod == "cod") {
                binding.tvSubtitle.text = "Pembayaran dilakukan saat datang"
                binding.tvInfoText.text = "Kami akan menghubungi kamu via WhatsApp untuk konfirmasi jadwal. Siapkan pembayaran saat datang ke lokasi."
            } else {
                binding.tvSubtitle.text = "Bukti transfer sedang diperiksa"
                binding.tvInfoText.text = "Tim kami akan memverifikasi pembayaran kamu. Status booking bisa dicek di halaman Riwayat."
            }
        }

        binding.btnViewHistory.setOnClickListener {
            findNavController().navigate(
                R.id.nav_history,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, false)
                    .setLaunchSingleTop(true)
                    .build()
            )
        }

        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(
                R.id.nav_home,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_home, true)
                    .setLaunchSingleTop(true)
                    .build()
            )
        }
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}