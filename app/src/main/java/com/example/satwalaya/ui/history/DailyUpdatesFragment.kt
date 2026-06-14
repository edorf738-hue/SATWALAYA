package com.example.satwalaya.ui.history

import android.app.Dialog
import android.graphics.Color
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
import com.example.satwalaya.databinding.FragmentDailyUpdatesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class DailyUpdatesFragment : Fragment() {
    private var _binding: FragmentDailyUpdatesBinding? = null
    private val binding get() = _binding!!

    private var bookingId = ""
    private var petNames = ""
    private var updatesListener: ListenerRegistration? = null

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
        updatesListener = FirebaseFirestore.getInstance()
            .collection("daily_updates")
            .whereEqualTo("bookingId", bookingId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { result, error ->
                if (_binding == null) return@addSnapshotListener
                if (error != null || result == null) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvUpdates.visibility = View.GONE
                    return@addSnapshotListener
                }

                if (result.isEmpty) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvUpdates.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvUpdates.visibility = View.VISIBLE

                    val updates = result.documents.map { doc ->
                        // Support data lama (photoUrl) dan data baru (mediaUrls)
                        val mediaUrls = doc.get("mediaUrls") as? List<*>
                        val photoUrl = doc.getString("photoUrl") ?: ""

                        val urls: List<String> = when {
                            mediaUrls != null && mediaUrls.isNotEmpty() ->
                                mediaUrls.mapNotNull { it?.toString() }
                            photoUrl.isNotEmpty() -> listOf(photoUrl)
                            else -> emptyList()
                        }

                        mapOf(
                            "date" to (doc.getString("date") ?: ""),
                            "dayCount" to (doc.getString("dayCount") ?: ""),
                            "notes" to (doc.getString("notes") ?: ""),
                            "staffName" to (doc.getString("staffName") ?: ""),
                            "urls" to urls
                        )
                    }

                    binding.rvUpdates.adapter = UpdatesAdapter(updates)
                }
            }
    }

    inner class UpdatesAdapter(
        private val updates: List<Map<String, Any>>
    ) : RecyclerView.Adapter<UpdatesAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val tvDayCount: TextView = view.findViewById(R.id.tvDayCount)
            val scrollPhotos: android.widget.HorizontalScrollView = view.findViewById(R.id.scrollPhotos)
            val photosContainer: LinearLayout = view.findViewById(R.id.photosContainer)
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
            holder.tvDayCount.text = update["dayCount"] as? String ?: ""
            holder.tvNotes.text = update["notes"] as? String ?: ""
            holder.tvStaff.text = "— ${update["staffName"]}"

            @Suppress("UNCHECKED_CAST")
            val urls = update["urls"] as? List<String> ?: emptyList()

            holder.photosContainer.removeAllViews()

            if (urls.isNotEmpty()) {
                holder.scrollPhotos.visibility = View.VISIBLE

                urls.forEach { url ->
                    val size = (180 * resources.displayMetrics.density).toInt()
                    val margin = (8 * resources.displayMetrics.density).toInt()

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

                    iv.setOnClickListener {
                        showFullscreenPhoto(url)
                    }

                    holder.photosContainer.addView(iv)
                }
            } else {
                holder.scrollPhotos.visibility = View.GONE
            }
        }

        override fun getItemCount() = updates.size

        private fun showFullscreenPhoto(url: String) {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            val imageView = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setBackgroundColor(Color.BLACK)
            }
            Glide.with(requireContext()).load(url).into(imageView)
            imageView.setOnClickListener { dialog.dismiss() }
            dialog.setContentView(imageView)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatesListener?.remove()
        updatesListener = null
        _binding = null
    }
}