package com.example.satwalaya.ui.hotel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.satwalaya.R
import com.example.satwalaya.data.model.ServiceItem
import com.example.satwalaya.ui.common.ServiceListAdapter
import com.example.satwalaya.databinding.FragmentHotelBinding

class HotelFragment : Fragment() {
    private var _binding: FragmentHotelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHotelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hotelServices = listOf(
            // Penginapan Standar
            ServiceItem(
                id = "hotel_kucing",
                name = "Penginapan Kucing",
                description = "Area khusus kucing yang nyaman dan aman.",
                imageResId = R.drawable.bg_service_icon_orange,
                price = "Rp 60.000",
                iconColor = R.color.bg_icon_blue
            ),
            ServiceItem(
                id = "hotel_anjing",
                name = "Penginapan Anjing",
                description = "Area luas khusus anjing aktif.",
                imageResId = R.drawable.bg_service_icon_blue,
                price = "Rp 70.000",
                iconColor = R.color.bg_icon_blue
            ),
            ServiceItem(
                id = "hotel_kelinci",
                name = "Penginapan Kelinci",
                description = "Kandang bersih dan tenang untuk kelinci.",
                imageResId = R.drawable.bg_service_icon_teal,
                price = "Rp 50.000",
                iconColor = R.color.bg_icon_blue
            ),

            // Penginapan VIP
            ServiceItem(
                id = "hotel_vip_kucing",
                name = "Penginapan VIP Kucing",
                description = "Kandang premium dengan AC dan kamera untuk kucing.",
                imageResId = R.drawable.bg_service_icon_purple,
                price = "Rp 150.000",
                iconColor = R.color.bg_icon_blue
            ),
            ServiceItem(
                id = "hotel_vip_anjing",
                name = "Penginapan VIP Anjing",
                description = "Kandang premium dengan AC dan kamera untuk anjing.",
                imageResId = R.drawable.bg_service_icon_purple,
                price = "Rp 180.000",
                iconColor = R.color.bg_icon_blue
            ),
            ServiceItem(
                id = "hotel_vip_kelinci",
                name = "Penginapan VIP Kelinci",
                description = "Kandang premium dengan AC dan kamera untuk kelinci.",
                imageResId = R.drawable.bg_service_icon_purple,
                price = "Rp 130.000",
                iconColor = R.color.bg_icon_blue
            ),

            // Penginapan Premium
            ServiceItem(
                id = "hotel_premium_kucing",
                name = "Penginapan Premium Kucing",
                description = "Fasilitas terbaik dengan perawatan ekstra untuk kucing.",
                imageResId = R.drawable.bg_service_icon_red,
                price = "Rp 200.000",
                iconColor = R.color.bg_icon_blue
            ),
            ServiceItem(
                id = "hotel_premium_anjing",
                name = "Penginapan Premium Anjing",
                description = "Fasilitas terbaik dengan perawatan ekstra untuk anjing.",
                imageResId = R.drawable.bg_service_icon_red,
                price = "Rp 250.000",
                iconColor = R.color.bg_icon_blue
            ),
            ServiceItem(
                id = "hotel_premium_kelinci",
                name = "Penginapan Premium Kelinci",
                description = "Fasilitas terbaik dengan perawatan ekstra untuk kelinci.",
                imageResId = R.drawable.bg_service_icon_red,
                price = "Rp 180.000",
                iconColor = R.color.bg_icon_blue
            )
        )

        binding.rvHotelServices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHotelServices.adapter = ServiceListAdapter(hotelServices) { service ->
            val bundle = Bundle().apply {
                putString("serviceName", service.name)
                putInt(
                    "servicePrice",
                    service.price.replace("Rp ", "").replace(".", "").toIntOrNull() ?: 0
                )
            }
            findNavController().navigate(R.id.action_hotelFragment_to_bookingFormFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}