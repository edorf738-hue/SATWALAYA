package com.example.satwalaya.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentHomeBinding
import com.example.satwalaya.ui.booking.BookingFragment
import com.example.satwalaya.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcomeName.text = "Halo, $name!"
        }

        viewModel.activeCount.observe(viewLifecycleOwner) { count ->
            binding.tvActiveCount.text = count.toString()
        }

        loadData()
        loadReviews()
        loadPets()

        binding.cardActive.setOnClickListener {
            findNavController().navigate(R.id.nav_history)
        }

        binding.cardHotel.setOnClickListener {
            BookingFragment.openTab = "hotel"
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav?.selectedItemId = R.id.nav_booking
        }

        binding.cardGrooming.setOnClickListener {
            BookingFragment.openTab = "grooming"
            val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav?.selectedItemId = R.id.nav_booking
        }

        binding.cardDataHewan.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_editProfileFragment)
        }

        binding.cardReviews.setOnClickListener {
            findNavController().navigate(R.id.reviewListFragment)
        }

        binding.btnAddPet.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_editProfileFragment)
        }

        binding.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_notificationsFragment)
        }

        loadNotifBadge()
    }

    private fun loadData() {
        viewModel.setUserName(sessionManager.getUsername())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.loadActiveBookings(userId)
    }

    private fun loadReviews() {
        val db = FirebaseFirestore.getInstance()

        db.collection("reviews").get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val avg = docs.map { it.getDouble("rating") ?: 0.0 }.average()
                    _binding?.tvRatingAvg?.text = String.format("%.1f", avg)
                }
            }

        db.collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (_binding == null) return@addOnSuccessListener
                if (!documents.isEmpty) {
                    val review = documents.first()
                    val name = review.getString("userName") ?: "Pengguna"
                    val comment = review.getString("reviewText") ?: ""
                    val rating = (review.getDouble("rating") ?: 5.0).toInt()
                    val stars = "⭐".repeat(rating)

                    _binding?.let { b ->
                        b.tvReviewerName.text = name
                        b.tvReviewText.text = comment
                        b.tvReviewStars.text = stars
                        b.tvReviewTime.text = "Baru saja"

                        // Load foto dari photoUrls array
                        @Suppress("UNCHECKED_CAST")
                        val photoUrls = review.get("photoUrls") as? List<String> ?: emptyList()

                        if (photoUrls.isNotEmpty()) {
                            b.scrollReviewPhotos.visibility = View.VISIBLE
                            b.reviewPhotosContainer.removeAllViews()

                            photoUrls.forEach { url ->
                                val size = (64 * resources.displayMetrics.density).toInt()
                                val margin = (6 * resources.displayMetrics.density).toInt()

                                val iv = ImageView(requireContext()).apply {
                                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                                        marginEnd = margin
                                    }
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                    setBackgroundResource(R.drawable.bg_input_field)
                                    clipToOutline = true
                                }

                                Glide.with(requireContext())
                                    .load(url)
                                    .centerCrop()
                                    .into(iv)

                                b.reviewPhotosContainer.addView(iv)
                            }
                        } else {
                            b.scrollReviewPhotos.visibility = View.GONE
                        }
                    }
                }
            }
    }

    private fun loadPets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("pets")
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val pet = documents.first()
                    val name = pet.getString("name") ?: "Hewan kamu"
                    val type = pet.getString("type") ?: ""
                    val breed = pet.getString("breed") ?: ""
                    val photoUrl = pet.getString("photoUrl") ?: ""
                    _binding?.let {
                        it.tvPetName.text = name
                        it.tvPetDetail.text = "$type • $breed"
                        if (photoUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.bg_pet_icon)
                                .into(it.ivPetPhoto)
                        }
                    }
                }
            }
    }

    private fun loadNotifBadge() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, _ ->
                val count = snapshots?.size() ?: 0
                _binding?.tvNotifBadge?.let { badge ->
                    if (count > 0) {
                        badge.visibility = View.VISIBLE
                        badge.text = if (count > 9) "9+" else count.toString()
                    } else {
                        badge.visibility = View.GONE
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        loadPets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}