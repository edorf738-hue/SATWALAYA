package com.example.satwalaya.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.satwalaya.ui.booking.BookingFragment

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
                if (!documents.isEmpty) {
                    val review = documents.first()
                    val name = review.getString("userName") ?: "Pengguna"
                    val comment = review.getString("reviewText") ?: ""
                    val rating = (review.getDouble("rating") ?: 5.0).toInt()
                    val stars = "⭐".repeat(rating)

                    _binding?.let {
                        it.tvReviewerName.text = name
                        it.tvReviewText.text = comment
                        it.tvReviewStars.text = stars
                        it.tvReviewTime.text = "Baru saja"
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