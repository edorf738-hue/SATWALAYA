package com.example.satwalaya.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentDailyUpdatesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DailyUpdatesFragment : Fragment() {
    private var _binding: FragmentDailyUpdatesBinding? = null
    private val binding get() = _binding!!

    private var bookingId = ""
    private var petNames = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDailyUpdatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            bookingId = it.getString("bookingId", "")
            petNames = it.getString("petNames", "")
        }

        binding.tvPetInfo.text = "Update harian untuk $petNames"
        binding.rvUpdates.layoutManager = LinearLayoutManager(requireContext())
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        loadUpdates()
    }

    private fun loadUpdates() {
        FirebaseFirestore.getInstance()
            .collection("daily_updates")
            .whereEqualTo("bookingId", bookingId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (_binding == null) return@addOnSuccessListener

                if (result.isEmpty) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvUpdates.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvUpdates.visibility = View.VISIBLE

                    val updates = result.documents.map { doc ->
                        mapOf(
                            "date" to (doc.getString("date") ?: ""),
                            "dayCount" to (doc.getString("dayCount") ?: ""),
                            "photoUrl" to (doc.getString("photoUrl") ?: ""),
                            "notes" to (doc.getString("notes") ?: ""),
                            "staffName" to (doc.getString("staffName") ?: "")
                        )
                    }

                    binding.rvUpdates.adapter = UpdatesAdapter(updates)
                }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                binding.emptyState.visibility = View.VISIBLE
                binding.rvUpdates.visibility = View.GONE
            }
    }

    // Adapter langsung di sini biar simpel
    inner class UpdatesAdapter(
        private val updates: List<Map<String, String>>
    ) : RecyclerView.Adapter<UpdatesAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val tvDayCount: TextView = view.findViewById(R.id.tvDayCount)
            val cardPhoto: androidx.cardview.widget.CardView = view.findViewById(R.id.cardPhoto)
            val ivPhoto: ImageView = view.findViewById(R.id.ivPhoto)
            val tvNotes: TextView = view.findViewById(R.id.tvNotes)
            val tvStaff: TextView = view.findViewById(R.id.tvStaff)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_update, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val update = updates[position]

            holder.tvDate.text = "📅 ${update["date"]}"
            holder.tvDayCount.text = update["dayCount"]
            holder.tvNotes.text = update["notes"]
            holder.tvStaff.text = "— ${update["staffName"]}"

            val photoUrl = update["photoUrl"] ?: ""
            if (photoUrl.isNotEmpty()) {
                holder.cardPhoto.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(photoUrl)
                    .centerCrop()
                    .into(holder.ivPhoto)
                holder.ivPhoto.setOnClickListener {
                    val dialog = android.app.Dialog(holder.itemView.context)
                    dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                    val imageView = android.widget.ImageView(holder.itemView.context)
                    imageView.layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    imageView.setBackgroundColor(android.graphics.Color.BLACK)
                    Glide.with(holder.itemView.context).load(photoUrl).into(imageView)
                    imageView.setOnClickListener { dialog.dismiss() }
                    dialog.setContentView(imageView)
                    dialog.window?.setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    dialog.show()
                }
            } else {
                holder.cardPhoto.visibility = View.GONE
            }
        }

        override fun getItemCount() = updates.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}