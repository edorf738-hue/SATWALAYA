package com.example.satwalaya.ui.booking

import android.graphics.Color
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

class BookingFragment : Fragment() {
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private var isHotelTab = true

    // Data paket Hotel
    private val hotelPackages = listOf(
        PackageData(
            name = "Reguler",
            badge = "Populer",
            badgeBgColor = R.drawable.bg_icon_green,
            badgeTextColor = R.color.green_dark,
            buttonBg = R.drawable.bg_icon_green,
            buttonTextColor = R.color.green_dark,
            features = listOf(
                "Kandang nyaman + AC",
                "Makan sesuai jadwal owner",
                "Area bermain dasar",
                "Daily updates foto"
            ),
            sizes = listOf(
                SizeData("Kecil", "< 5 kg", 60000),
                SizeData("Sedang", "5-15 kg", 75000),
                SizeData("Besar", "> 15 kg", 90000)
            )
        ),
        PackageData(
            name = "Premium",
            badge = "Best value",
            badgeBgColor = R.drawable.bg_icon_amber,
            badgeTextColor = R.color.amber_dark,
            buttonBg = R.drawable.bg_icon_amber,
            buttonTextColor = R.color.amber_dark,
            features = listOf(
                "Semua fitur Reguler",
                "Kandang lebih luas",
                "Area bermain lengkap",
                "CCTV 24 jam (soon)"
            ),
            sizes = listOf(
                SizeData("Kecil", "< 5 kg", 120000),
                SizeData("Sedang", "5-15 kg", 150000),
                SizeData("Besar", "> 15 kg", 180000)
            )
        )
    )

    // Data paket Grooming
    private val groomingPackages = listOf(
        PackageData(
            name = "Paket Fresh",
            badge = "Populer",
            badgeBgColor = R.drawable.bg_icon_green,
            badgeTextColor = R.color.green_dark,
            buttonBg = R.drawable.bg_pet_icon,
            buttonTextColor = R.color.purple_primary,
            features = listOf(
                "Mandi bersih",
                "Keringkan bulu",
                "Parfum"
            ),
            sizes = listOf(
                SizeData("Kecil", "< 5 kg", 40000),
                SizeData("Sedang", "5-15 kg", 55000),
                SizeData("Besar", "> 15 kg", 70000)
            )
        ),
        PackageData(
            name = "Full Grooming",
            badge = "Best value",
            badgeBgColor = R.drawable.bg_icon_amber,
            badgeTextColor = R.color.amber_dark,
            buttonBg = R.drawable.bg_pet_icon,
            buttonTextColor = R.color.purple_primary,
            features = listOf(
                "Semua fitur Paket Fresh",
                "Potong dan rapikan bulu",
                "Potong kuku",
                "Bersihkan telinga"
            ),
            sizes = listOf(
                SizeData("Kecil", "< 5 kg", 85000),
                SizeData("Sedang", "5-15 kg", 100000),
                SizeData("Besar", "> 15 kg", 120000)
            )
        )
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showPackages(isHotel = true)

        binding.tabHotel.setOnClickListener {
            if (!isHotelTab) {
                isHotelTab = true
                updateTabs()
                showPackages(isHotel = true)
            }
        }

        binding.tabGrooming.setOnClickListener {
            if (isHotelTab) {
                isHotelTab = false
                updateTabs()
                showPackages(isHotel = false)
            }
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

    private fun showPackages(isHotel: Boolean) {
        binding.packageContainer.removeAllViews()
        val packages = if (isHotel) hotelPackages else groomingPackages

        packages.forEach { pkg ->
            val cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_package_card, binding.packageContainer, false)

            val tvName = cardView.findViewById<TextView>(R.id.tvPackageName)
            val tvPrice = cardView.findViewById<TextView>(R.id.tvPackagePrice)
            val tvBadge = cardView.findViewById<TextView>(R.id.tvBadge)
            val featuresContainer = cardView.findViewById<LinearLayout>(R.id.featuresContainer)
            val btnBook = cardView.findViewById<TextView>(R.id.btnBook)

            tvName.text = pkg.name
            tvPrice.text = "Mulai ${formatPrice(pkg.sizes[0].price)}" + if (isHotel) "/malam" else ""
            tvBadge.text = pkg.badge
            tvBadge.setBackgroundResource(pkg.badgeBgColor)
            tvBadge.setTextColor(resources.getColor(pkg.badgeTextColor, null))
            btnBook.setBackgroundResource(pkg.buttonBg)
            btnBook.setTextColor(resources.getColor(pkg.buttonTextColor, null))

            // Tambah features
            pkg.features.forEach { feature ->
                val tv = TextView(requireContext()).apply {
                    text = "✓  $feature"
                    textSize = 11f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(0, 4, 0, 4)
                }
                featuresContainer.addView(tv)
            }

            btnBook.setOnClickListener {
                val serviceName = if (isHotel) "Hotel ${pkg.name}" else pkg.name
                val bundle = Bundle().apply {
                    putString("serviceName", serviceName)
                    putInt("servicePrice", pkg.sizes[0].price)
                    putString("packageSizes", pkg.sizes.joinToString(";") { "${it.label},${it.weightRange},${it.price}" })
                }
                findNavController().navigate(R.id.action_nav_booking_to_bookingFormFragment, bundle)
            }

            binding.packageContainer.addView(cardView)
        }
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Data classes
    data class PackageData(
        val name: String,
        val badge: String,
        val badgeBgColor: Int,
        val badgeTextColor: Int,
        val buttonBg: Int,
        val buttonTextColor: Int,
        val features: List<String>,
        val sizes: List<SizeData>
    )

    data class SizeData(
        val label: String,
        val weightRange: String,
        val price: Int
    )
}