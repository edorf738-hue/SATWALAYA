package com.example.satwalaya.ui.review

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentReviewListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReviewListFragment : Fragment() {
    private var _binding: FragmentReviewListBinding? = null
    private val binding get() = _binding!!
    private var reviewsListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReviewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        loadReviews()
    }

    private fun loadReviews() {
        reviewsListener = FirebaseFirestore.getInstance()
            .collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { documents, error ->
                if (error != null || documents == null || _binding == null) return@addSnapshotListener

                val reviews: List<Map<String, Any?>> = documents.map { doc ->
                    mapOf(
                        "id" to doc.id,
                        "userName" to (doc.getString("userName") ?: "User"),
                        "userPhotoUrl" to (doc.getString("userPhotoUrl") ?: ""),
                        "rating" to (doc.getDouble("rating") ?: 0.0),
                        "reviewText" to (doc.getString("reviewText") ?: ""),
                        "serviceName" to (doc.getString("serviceName") ?: ""),
                        "photoUrls" to ((doc.get("photoUrls") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList<String>()),
                        "createdAt" to doc.getTimestamp("createdAt")
                    )
                }

                if (reviews.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvReviews.visibility = View.GONE
                    return@addSnapshotListener
                }

                val avg = reviews.map { (it["rating"] as Double) }.average()
                binding.tvAvgRating.text = String.format("%.1f", avg)
                binding.tvStarsAvg.text = "⭐".repeat(avg.toInt())
                binding.tvTotalReviews.text = "${reviews.size} ulasan"
                binding.emptyState.visibility = View.GONE
                binding.rvReviews.visibility = View.VISIBLE
                binding.rvReviews.adapter = ReviewAdapter(reviews)
            }
    }

    private fun getRelativeTime(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val now = System.currentTimeMillis()
        val diff = now - timestamp.toDate().time
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Baru saja"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} menit lalu"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} jam lalu"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} hari lalu"
            diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7} minggu lalu"
            diff < TimeUnit.DAYS.toMillis(365) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 30} bulan lalu"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 365} tahun lalu"
        }
    }

    inner class ReviewAdapter(
        private val reviews: List<Map<String, Any?>>
    ) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivAvatar: ImageView = view.findViewById(R.id.ivReviewerAvatar)
            val tvName: TextView = view.findViewById(R.id.tvReviewerName)
            val tvTime: TextView = view.findViewById(R.id.tvReviewTime)
            val tvText: TextView = view.findViewById(R.id.tvReviewText)
            val tvStars: TextView = view.findViewById(R.id.tvStars)
            val tvService: TextView = view.findViewById(R.id.tvServiceReview)
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

            holder.tvName.text = review["userName"] as String
            holder.tvText.text = review["reviewText"] as String
            holder.tvStars.text = "⭐".repeat((review["rating"] as Double).toInt())
            holder.tvService.text = review["serviceName"] as String
            holder.tvTime.text = getRelativeTime(review["createdAt"] as? com.google.firebase.Timestamp)

            // Load foto profil user
            val userPhotoUrl = review["userPhotoUrl"] as? String ?: ""
            if (userPhotoUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(userPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(holder.ivAvatar)
            }

            // Load foto review
            val photoUrls = review["photoUrls"] as? List<String> ?: emptyList()
            holder.photosContainer.removeAllViews()

            if (photoUrls.isNotEmpty()) {
                holder.scrollPhotos.visibility = View.VISIBLE
                photoUrls.forEach { url ->
                    val size = (80 * holder.itemView.context.resources.displayMetrics.density).toInt()
                    val margin = (6 * holder.itemView.context.resources.displayMetrics.density).toInt()

                    val iv = ImageView(holder.itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(size, size).apply {
                            marginEnd = margin
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setBackgroundResource(R.drawable.bg_input_field)
                        clipToOutline = true
                    }

                    Glide.with(holder.itemView.context).load(url).centerCrop().into(iv)

                    // Klik foto → fullscreen
                    iv.setOnClickListener {
                        val dialog = Dialog(holder.itemView.context)
                        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                        val imageView = ImageView(holder.itemView.context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                        Glide.with(holder.itemView.context).load(url).into(imageView)
                        imageView.setOnClickListener { dialog.dismiss() }
                        dialog.setContentView(imageView)
                        dialog.window?.setLayout(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        reviewsListener?.remove()
        reviewsListener = null
        _binding = null
    }
}