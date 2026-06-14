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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import android.widget.TextView

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: HomeViewModel by viewModels()
    private var petsListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null
    private var notifBadgeListener: ListenerRegistration? = null

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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.setUserName(sessionManager.getUsername().ifEmpty { "Pet Owner" })
        viewModel.loadActiveBookings(userId)
        userListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .addSnapshotListener { doc, _ ->
                if (_binding == null) return@addSnapshotListener
                val name = doc?.getString("name")?.ifEmpty { null }
                    ?: sessionManager.getUsername().ifEmpty { "Pet Owner" }
                viewModel.setUserName(name)
            }
    }

    private fun loadReviews() {
        val db = FirebaseFirestore.getInstance()

        // Hitung rata-rata rating
        db.collection("reviews").get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val avg = docs.map { it.getDouble("rating") ?: 0.0 }.average()
                    _binding?.tvRatingAvg?.text = String.format("%.1f", avg)
                }
            }

        // Load max 3 review terbaru
        db.collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                if (_binding == null) return@addOnSuccessListener
                if (documents.isEmpty) return@addOnSuccessListener

                val reviews: List<Map<String, Any?>> = documents.map { doc ->
                    mapOf(
                        "userName" to (doc.getString("userName") ?: "Pengguna"),
                        "userPhotoUrl" to (doc.getString("userPhotoUrl") ?: ""),
                        "rating" to (doc.getDouble("rating") ?: 5.0),
                        "reviewText" to (doc.getString("reviewText") ?: ""),
                        "serviceName" to (doc.getString("serviceName") ?: ""),
                        "photoUrls" to ((doc.get("photoUrls") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList<String>()),
                        "createdAt" to doc.getTimestamp("createdAt")
                    )
                }

                _binding?.rvHomeReviews?.apply {
                    layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
                    adapter = HomeReviewAdapter(reviews)
                }
            }
    }

    inner class HomeReviewAdapter(
        private val reviews: List<Map<String, Any?>>
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<HomeReviewAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val ivAvatar: ImageView = view.findViewById(R.id.ivReviewerAvatar)
            val tvName: TextView = view.findViewById(R.id.tvReviewerName)
            val tvTime: TextView = view.findViewById(R.id.tvReviewTime)
            val tvStars: TextView = view.findViewById(R.id.tvStars)
            val tvService: TextView = view.findViewById(R.id.tvServiceReview)
            val tvText: TextView = view.findViewById(R.id.tvReviewText)
            val scrollPhotos: android.widget.HorizontalScrollView = view.findViewById(R.id.scrollReviewPhotos)
            val photosContainer: LinearLayout = view.findViewById(R.id.reviewPhotosContainer)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_review_home, parent, false)
            return ViewHolder(view)
        }

        @Suppress("UNCHECKED_CAST")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val review = reviews[position]

            holder.tvName.text = review["userName"] as? String ?: ""
            holder.tvText.text = review["reviewText"] as? String ?: ""
            holder.tvStars.text = "⭐".repeat((review["rating"] as? Double ?: 0.0).toInt())
            holder.tvService.text = review["serviceName"] as? String ?: ""

            // Waktu relatif
            val timestamp = review["createdAt"] as? com.google.firebase.Timestamp
            holder.tvTime.text = if (timestamp != null) {
                val diff = System.currentTimeMillis() - timestamp.toDate().time
                when {
                    diff < 60_000L -> "Baru saja"
                    diff < 3_600_000L -> "${diff / 60_000} menit lalu"
                    diff < 86_400_000L -> "${diff / 3_600_000} jam lalu"
                    diff < 604_800_000L -> "${diff / 86_400_000} hari lalu"
                    diff < 2_592_000_000L -> "${diff / 604_800_000} minggu lalu"
                    else -> "${diff / 2_592_000_000L} bulan lalu"
                }
            } else ""

            // Foto profil user
            val userPhotoUrl = review["userPhotoUrl"] as? String ?: ""
            if (userPhotoUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(userPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(holder.ivAvatar)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.bg_avatar_circle)
            }

            // Foto review
            val photoUrls = review["photoUrls"] as? List<String> ?: emptyList()
            holder.photosContainer.removeAllViews()
            if (photoUrls.isNotEmpty()) {
                holder.scrollPhotos.visibility = View.VISIBLE
                photoUrls.forEach { url ->
                    val size = (64 * holder.itemView.context.resources.displayMetrics.density).toInt()
                    val margin = (6 * holder.itemView.context.resources.displayMetrics.density).toInt()
                    val iv = ImageView(holder.itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = margin }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setBackgroundResource(R.drawable.bg_input_field)
                        clipToOutline = true
                    }
                    Glide.with(holder.itemView.context).load(url).centerCrop().into(iv)
                    iv.setOnClickListener {
                        val dialog = android.app.Dialog(holder.itemView.context)
                        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                        val imageView = ImageView(holder.itemView.context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                        Glide.with(holder.itemView.context).load(url).into(imageView)
                        imageView.setOnClickListener { dialog.dismiss() }
                        dialog.setContentView(imageView)
                        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        dialog.show()
                    }
                    holder.photosContainer.addView(iv)
                }
            } else {
                holder.scrollPhotos.visibility = View.GONE
            }
        }

        override fun getItemCount() = reviews.size
    }

    private fun loadPets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        petsListener = FirebaseFirestore.getInstance()
            .collection("pets")
            .whereEqualTo("userId", userId)
            .limit(1)
            .addSnapshotListener { documents, _ ->
                if (_binding == null) return@addSnapshotListener
                if (documents != null && !documents.isEmpty) {
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
        notifBadgeListener = FirebaseFirestore.getInstance()
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

    override fun onDestroyView() {
        super.onDestroyView()
        petsListener?.remove()
        petsListener = null
        userListener?.remove()
        userListener = null
        notifBadgeListener?.remove()
        notifBadgeListener = null
        _binding = null
    }
}