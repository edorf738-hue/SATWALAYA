package com.example.satwalaya.ui.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentBookingBinding
import com.google.firebase.firestore.FirebaseFirestore

class BookingFragment : Fragment() {
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!
    private var isHotelTab = true

    companion object {
        var openTab = "hotel"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPackages(isHotel = true)

        binding.tabHotel.setOnClickListener {
            if (!isHotelTab) {
                isHotelTab = true
                updateTabs()
                loadPackages(isHotel = true)
            }
        }

        binding.tabGrooming.setOnClickListener {
            if (isHotelTab) {
                isHotelTab = false
                updateTabs()
                loadPackages(isHotel = false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (openTab == "grooming") {
            isHotelTab = false
            updateTabs()
            loadPackages(isHotel = false)
        } else {
            isHotelTab = true
            updateTabs()
            loadPackages(isHotel = true)
        }
        openTab = "hotel"
    }

    private fun loadPackages(isHotel: Boolean) {
        val category = if (isHotel) "hotel" else "grooming"
        binding.packageContainer.removeAllViews()

        FirebaseFirestore.getInstance()
            .collection("services")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                val packages = documents.sortedBy { it.getString("name") }
                packages.forEach { doc ->
                    val name = doc.getString("name") ?: ""
                    val badge = doc.getString("badge") ?: ""
                    val features = (doc.get("features") as? List<*>)?.map { it.toString() } ?: emptyList()
                    val sizesMap = doc.get("sizes") as? Map<*, *> ?: emptyMap<String, Any>()

                    val sizes = listOf("kecil", "sedang", "besar").mapNotNull { key ->
                        val sizeData = sizesMap[key] as? Map<*, *> ?: return@mapNotNull null
                        SizeData(
                            label = sizeData["label"]?.toString() ?: key,
                            weightRange = sizeData["weightRange"]?.toString() ?: "",
                            price = (sizeData["price"] as? Long)?.toInt() ?: 0
                        )
                    }

                    if (sizes.isEmpty()) return@forEach

                    val cardView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_package_card, binding.packageContainer, false)

                    val tvName = cardView.findViewById<TextView>(R.id.tvPackageName)
                    val tvPrice = cardView.findViewById<TextView>(R.id.tvPackagePrice)
                    val tvBadge = cardView.findViewById<TextView>(R.id.tvBadge)
                    val featuresContainer = cardView.findViewById<LinearLayout>(R.id.featuresContainer)
                    val btnBook = cardView.findViewById<TextView>(R.id.btnBook)

                    tvName.text = name
                    tvPrice.text = "Mulai ${formatPrice(sizes[0].price)}" + if (isHotel) "/malam" else ""
                    tvBadge.text = badge

                    if (badge == "Populer") {
                        tvBadge.setBackgroundResource(R.drawable.bg_icon_green)
                        tvBadge.setTextColor(resources.getColor(R.color.green_dark, null))
                        btnBook.setBackgroundResource(R.drawable.bg_icon_green)
                        btnBook.setTextColor(resources.getColor(R.color.green_dark, null))
                    } else {
                        tvBadge.setBackgroundResource(R.drawable.bg_icon_amber)
                        tvBadge.setTextColor(resources.getColor(R.color.amber_dark, null))
                        btnBook.setBackgroundResource(if (isHotel) R.drawable.bg_icon_amber else R.drawable.bg_pet_icon)
                        btnBook.setTextColor(resources.getColor(if (isHotel) R.color.amber_dark else R.color.purple_primary, null))
                    }

                    features.forEach { feature ->
                        val tv = TextView(requireContext()).apply {
                            text = "✓  $feature"
                            textSize = 11f
                            setTextColor(resources.getColor(R.color.text_secondary, null))
                            setPadding(0, 4, 0, 4)
                        }
                        featuresContainer.addView(tv)
                    }

                    btnBook.setOnClickListener {
                        val serviceName = if (isHotel) "Hotel $name" else name
                        val bundle = Bundle().apply {
                            putString("serviceName", serviceName)
                            putInt("servicePrice", sizes[0].price)
                            putString("packageSizes", sizes.joinToString(";") {
                                "${it.label},${it.weightRange},${it.price}"
                            })
                        }
                        findNavController().navigate(R.id.action_nav_booking_to_bookingFormFragment, bundle)
                    }

                    binding.packageContainer.addView(cardView)
                }
            }
            .addOnFailureListener {
                // gagal load
            }
    }

    private fun updateTabs() {
        if (isHotelTab) {
            binding.tabHotel.setBackgroundResource(R.drawable.bg_icon_green)
            binding.tabHotel.setTextColor(resources.getColor(R.color.green_dark, null))
            binding.tabGrooming.setBackgroundResource(R.drawable.bg_input_field)
            binding.tabGrooming.setTextColor(resources.getColor(R.color.text_secondary, null))
        } else {
            binding.tabGrooming.setBackgroundResource(R.drawable.bg_pet_icon)
            binding.tabGrooming.setTextColor(resources.getColor(R.color.purple_primary, null))
            binding.tabHotel.setBackgroundResource(R.drawable.bg_input_field)
            binding.tabHotel.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class SizeData(
        val label: String,
        val weightRange: String,
        val price: Int
    )
}