package com.example.satwalaya.ui.booking

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.data.model.Pet
import com.example.satwalaya.data.repository.PetRepository
import com.example.satwalaya.databinding.FragmentBookingFormBinding
import com.example.satwalaya.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class BookingFormFragment : Fragment() {
    private var _binding: FragmentBookingFormBinding? = null
    private val binding get() = _binding!!
    private val petRepository = PetRepository()
    private lateinit var sessionManager: SessionManager

    private var serviceName = ""
    private var basePrice = 0
    private var isHotel = false
    private var sizesPriceMap = mutableMapOf<String, Int>()

    private val groomFreshPrices = mapOf("Kecil" to 40000, "Sedang" to 55000, "Besar" to 70000)
    private val groomFullPrices = mapOf("Kecil" to 85000, "Sedang" to 100000, "Besar" to 120000)

    private val selectedPets = mutableListOf<Pet>()
    private var allPets = listOf<Pet>()
    private var checkInDate: Calendar? = null
    private var checkOutDate: Calendar? = null
    private var addGrooming = false
    private var groomingType = "fresh"

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookingFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        arguments?.let {
            serviceName = it.getString("serviceName", "")
            basePrice = it.getInt("servicePrice", 0)
            val sizesStr = it.getString("packageSizes", "")
            parseSizes(sizesStr)
        }

        isHotel = serviceName.contains("Hotel", ignoreCase = true)

        binding.tvBannerName.text = serviceName
        if (isHotel) {
            binding.tvBannerPrice.text = "Mulai ${formatPrice(basePrice)}/malam"
            binding.packageBanner.setBackgroundResource(R.drawable.bg_icon_green)
            binding.tvBannerName.setTextColor(resources.getColor(R.color.green_dark, null))
            binding.tvBannerPrice.setTextColor(resources.getColor(R.color.green_dark, null))
        } else {
            binding.tvBannerPrice.text = "Mulai ${formatPrice(basePrice)}"
            binding.packageBanner.setBackgroundResource(R.drawable.bg_pet_icon)
            binding.tvBannerName.setTextColor(resources.getColor(R.color.purple_primary, null))
            binding.tvBannerPrice.setTextColor(resources.getColor(R.color.purple_primary, null))
        }

        if (isHotel) {
            binding.addonSection.visibility = View.VISIBLE
            binding.cardCheckOut.visibility = View.VISIBLE
            binding.tvCheckInLabel.text = "Check-in"
        } else {
            binding.addonSection.visibility = View.GONE
            binding.cardCheckOut.visibility = View.GONE
            binding.tvCheckInLabel.text = "Tanggal appointment"
        }

        binding.tvOwnerName.text = sessionManager.getUsername().ifEmpty { "Pemilik Hewan" }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnAddPetFromBooking.setOnClickListener {
            findNavController().navigate(R.id.addPetFragment)
        }

        setupDatePickers()
        setupAddonSection()
        loadPets()

        binding.btnSubmit.setOnClickListener { submitBooking() }
    }

    private fun parseSizes(sizesStr: String) {
        if (sizesStr.isEmpty()) return
        sizesStr.split(";").forEach { part ->
            val segments = part.split(",")
            if (segments.size >= 3) {
                sizesPriceMap[segments[0]] = segments[2].toIntOrNull() ?: 0
            }
        }
    }

    private fun getSizeCategory(weightStr: String): String {
        val weight = weightStr.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
        return when {
            weight < 5 -> "Kecil"
            weight <= 15 -> "Sedang"
            else -> "Besar"
        }
    }

    private fun getPriceForSize(size: String): Int {
        return sizesPriceMap[size] ?: basePrice
    }

    private fun loadPets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        petRepository.getPets(userId,
            onSuccess = { pets ->
                allPets = pets
                if (pets.isEmpty()) {
                    binding.petEmptyState.visibility = View.VISIBLE
                    binding.petListContainer.visibility = View.GONE
                    binding.tvPetInfo.visibility = View.GONE
                } else {
                    binding.petEmptyState.visibility = View.GONE
                    binding.petListContainer.visibility = View.VISIBLE
                    binding.tvPetInfo.visibility = View.VISIBLE
                    displayPets(pets)
                }
            },
            onFailure = {
                binding.petEmptyState.visibility = View.VISIBLE
                binding.petListContainer.visibility = View.GONE
            }
        )
    }

    private fun displayPets(pets: List<Pet>) {
        binding.petListContainer.removeAllViews()

        pets.forEach { pet ->
            val card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_pet_card, binding.petListContainer, false)

            // ← DIGANTI: pakai ImageView bukan TextView
            val ivPhoto = card.findViewById<ImageView>(R.id.ivPetPhoto)
            val tvName = card.findViewById<TextView>(R.id.tvPetName)
            val tvDetail = card.findViewById<TextView>(R.id.tvPetDetail)
            val tvWeight = card.findViewById<TextView>(R.id.tvPetWeight)
            val tvVaccine = card.findViewById<TextView>(R.id.tvPetVaccine)
            val tvArrow = card.findViewById<TextView>(R.id.tvArrow)
            tvArrow.visibility = View.GONE

            val size = getSizeCategory(pet.weight)

            // Load foto atau tampilkan emoji sebagai fallback
            if (pet.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(pet.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.bg_pet_icon)
                    .into(ivPhoto)
            } else {
                // Kalau tidak ada foto, tampilkan background sesuai tipe
                ivPhoto.setImageResource(R.drawable.bg_pet_icon)
            }

            tvName.text = pet.name
            tvDetail.text = "${pet.type} ${pet.breed}, ${pet.age}"
            tvWeight.text = "${pet.weight} kg"
            tvVaccine.text = "Ukuran: $size"

            val isSelected = selectedPets.any { it.id == pet.id }
            updateCardSelection(card, isSelected)

            card.setOnClickListener {
                val alreadySelected = selectedPets.any { it.id == pet.id }
                if (alreadySelected) {
                    selectedPets.removeAll { it.id == pet.id }
                    updateCardSelection(card, false)
                } else {
                    if (selectedPets.size >= 5) {
                        Toast.makeText(requireContext(),
                            "Maksimal 5 hewan per booking", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    selectedPets.add(pet)
                    updateCardSelection(card, true)
                }
                binding.tvPetCounter.text = "${selectedPets.size}/5 dipilih"
                calculatePrice()
            }

            binding.petListContainer.addView(card)
        }
    }

    private fun updateCardSelection(card: View, selected: Boolean) {
        if (selected) {
            card.alpha = 1f
            card.setBackgroundResource(R.drawable.bg_pet_icon)
        } else {
            card.alpha = 0.7f
            card.background = null
        }
    }

    private fun setupDatePickers() {
        binding.cardCheckIn.setOnClickListener {
            showDatePicker { date ->
                checkInDate = date
                binding.tvCheckInDate.text = dateFormat.format(date.time)
                binding.tvCheckInDate.setTextColor(resources.getColor(R.color.text_primary, null))

                if (isHotel && checkOutDate == null) {
                    checkOutDate = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
                    binding.tvCheckOutDate.text = dateFormat.format(checkOutDate!!.time)
                    binding.tvCheckOutDate.setTextColor(resources.getColor(R.color.text_primary, null))
                }
                calculatePrice()
            }
        }

        binding.cardCheckOut.setOnClickListener {
            showDatePicker { date ->
                if (checkInDate != null && date.before(checkInDate)) {
                    Toast.makeText(requireContext(),
                        "Check-out harus setelah check-in", Toast.LENGTH_SHORT).show()
                    return@showDatePicker
                }
                checkOutDate = date
                binding.tvCheckOutDate.text = dateFormat.format(date.time)
                binding.tvCheckOutDate.setTextColor(resources.getColor(R.color.text_primary, null))
                calculatePrice()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val selected = Calendar.getInstance().apply { set(year, month, day) }
            onDateSelected(selected)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun setupAddonSection() {
        binding.cbAddGrooming.setOnCheckedChangeListener { _, isChecked ->
            addGrooming = isChecked
            binding.groomingOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked && binding.rgGrooming.checkedRadioButtonId == -1) {
                binding.rbFresh.isChecked = true
            }
            calculatePrice()
        }

        binding.rgGrooming.setOnCheckedChangeListener { _, checkedId ->
            groomingType = if (checkedId == R.id.rbFresh) "fresh" else "full"
            calculatePrice()
        }
    }

    private fun calculatePrice() {
        binding.priceBreakdown.removeAllViews()

        if (selectedPets.isEmpty() || checkInDate == null) {
            binding.tvPriceEmpty.visibility = View.VISIBLE
            binding.totalRow.visibility = View.GONE
            binding.dividerTotal.visibility = View.GONE
            return
        }

        if (isHotel && checkOutDate == null) {
            binding.tvPriceEmpty.visibility = View.VISIBLE
            binding.totalRow.visibility = View.GONE
            binding.dividerTotal.visibility = View.GONE
            return
        }

        binding.tvPriceEmpty.visibility = View.GONE
        binding.totalRow.visibility = View.VISIBLE
        binding.dividerTotal.visibility = View.VISIBLE

        val nights = if (isHotel && checkInDate != null && checkOutDate != null) {
            val diff = checkOutDate!!.timeInMillis - checkInDate!!.timeInMillis
            (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } else 1

        var total = 0

        selectedPets.forEach { pet ->
            val size = getSizeCategory(pet.weight)
            val price = getPriceForSize(size)

            if (isHotel) {
                val subtotal = price * nights
                addPriceRow("${pet.name} (${pet.weight} kg • $size) × $nights malam", formatPrice(subtotal))
                total += subtotal
            } else {
                addPriceRow("${pet.name} (${pet.weight} kg • $size)", formatPrice(price))
                total += price
            }

            if (addGrooming && isHotel) {
                val groomPrices = if (groomingType == "fresh") groomFreshPrices else groomFullPrices
                val groomPrice = groomPrices[size] ?: 0
                val groomName = if (groomingType == "fresh") "Paket Fresh" else "Full Grooming"
                addPriceRow("  + $groomName (${pet.name})", formatPrice(groomPrice))
                total += groomPrice
            }
        }

        binding.tvTotalPrice.text = formatPrice(total)
    }

    private fun addPriceRow(label: String, value: String) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 6, 0, 6)
        }

        val tvLabel = TextView(requireContext()).apply {
            text = label
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvValue = TextView(requireContext()).apply {
            text = value
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_primary, null))
        }

        row.addView(tvLabel)
        row.addView(tvValue)
        binding.priceBreakdown.addView(row)
    }

    private fun submitBooking() {
        if (selectedPets.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih minimal 1 hewan", Toast.LENGTH_SHORT).show()
            return
        }
        if (checkInDate == null) {
            Toast.makeText(requireContext(), "Pilih tanggal", Toast.LENGTH_SHORT).show()
            return
        }
        if (isHotel && checkOutDate == null) {
            Toast.makeText(requireContext(), "Pilih tanggal check-out", Toast.LENGTH_SHORT).show()
            return
        }
        val whatsApp = binding.etWhatsApp.text.toString().trim()
        if (whatsApp.isEmpty()) {
            Toast.makeText(requireContext(), "Isi nomor WhatsApp", Toast.LENGTH_SHORT).show()
            return
        }

        val petNames = selectedPets.joinToString(", ") { it.name }
        val totalPrice = calculateTotalPrice()

        val bundle = Bundle().apply {
            putString("serviceName", serviceName)
            putString("ownerName", binding.tvOwnerName.text.toString())
            putString("ownerPhone", whatsApp)
            putString("petNames", petNames)
            putString("checkIn", dateFormat.format(checkInDate!!.time))
            putString("checkOut", if (isHotel) dateFormat.format(checkOutDate!!.time) else "-")
            putInt("totalPrice", totalPrice)
            putBoolean("addGrooming", addGrooming)
            putString("groomingType", if (addGrooming) groomingType else "")
        }

        findNavController().navigate(R.id.action_bookingFormFragment_to_orderReviewFragment, bundle)
    }

    private fun calculateTotalPrice(): Int {
        val nights = if (isHotel && checkInDate != null && checkOutDate != null) {
            val diff = checkOutDate!!.timeInMillis - checkInDate!!.timeInMillis
            (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } else 1

        var total = 0
        selectedPets.forEach { pet ->
            val size = getSizeCategory(pet.weight)
            val price = getPriceForSize(size)
            total += if (isHotel) price * nights else price

            if (addGrooming && isHotel) {
                val groomPrices = if (groomingType == "fresh") groomFreshPrices else groomFullPrices
                total += groomPrices[size] ?: 0
            }
        }
        return total
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}