package com.example.satwalaya.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentReviewListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReviewListFragment : Fragment() {
    private var _binding: FragmentReviewListBinding? = null
    private val binding get() = _binding!!

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
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reviews = documents.map { doc ->
                    mapOf(
                        "userName" to (doc.getString("userName") ?: "User"),
                        "rating" to (doc.getDouble("rating") ?: 0.0),
                        "reviewText" to (doc.getString("reviewText") ?: ""),
                        "serviceName" to (doc.getString("serviceName") ?: "")
                    )
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

    inner class ReviewAdapter(
        private val reviews: List<Map<String, Any>>
    ) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvReviewerName)
            val tvText: TextView = view.findViewById(R.id.tvReviewText)
            val tvStars: TextView = view.findViewById(R.id.tvStars)
            val tvService: TextView = view.findViewById(R.id.tvServiceReview)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_review_home, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val review = reviews[position]
            holder.tvName.text = review["userName"] as String
            holder.tvText.text = review["reviewText"] as String
            holder.tvStars.text = "⭐".repeat((review["rating"] as Double).toInt())
            holder.tvService.text = review["serviceName"] as String
        }

        override fun getItemCount() = reviews.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}