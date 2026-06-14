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
import com.google.firebase.firestore.ListenerRegistration

class BookingFragment : Fragment() {
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!
    private var isHotelTab = true
    private var servicesListener: ListenerRegistration? = null

    companion object {
        var openTab = "hotel"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val requestedGrooming = openTab == "grooming"
        openTab = "hotel"
        if (requestedGrooming) {
            isHotelTab = false
        }
        updateTabs()
        loadPackages(isHotel = isHotelTab)
    }

    private fun loadPackages(isHotel: Boolean) {
        val category = if (isHotel) "hotel" else "grooming"
        servicesListener?.remove()

        servicesListener = FirebaseFirestore.getInstance()
            .collection("services")
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null || _binding == null) return@addSnapshotListener
                binding.packageContainer.removeAllViews()
                val packages = snapshots.sortedBy { it.getString("name") }
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
    }

    private fun updateTabs() {
        if (isHotelTab) {
            binding.tabHotel.setBackgroundResource(R.drawable.bg_tab_active)
            binding.tabHotel.setTextColor(android.graphics.Color.parseColor("#6A5AE0"))
            binding.tabHotel.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.tabGrooming.setBackgroundResource(R.drawable.bg_tab_inactive)
            binding.tabGrooming.setTextColor(android.graphics.Color.parseColor("#CCffffff"))
            binding.tabGrooming.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            binding.tabGrooming.setBackgroundResource(R.drawable.bg_tab_active)
            binding.tabGrooming.setTextColor(android.graphics.Color.parseColor("#6A5AE0"))
            binding.tabGrooming.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.tabHotel.setBackgroundResource(R.drawable.bg_tab_inactive)
            binding.tabHotel.setTextColor(android.graphics.Color.parseColor("#CCffffff"))
            binding.tabHotel.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        servicesListener?.remove()
        servicesListener = null
        _binding = null
    }

    data class SizeData(
        val label: String,
        val weightRange: String,
        val price: Int
    )
}