package com.example.satwalaya.ui.pet

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.data.model.Pet

class PetAdapter(
    private var pets: List<Pet>,
    private val onPetClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPetPhoto: ImageView = view.findViewById(R.id.ivPetPhoto) // ← DIGANTI
        val tvPetName: TextView = view.findViewById(R.id.tvPetName)
        val tvPetDetail: TextView = view.findViewById(R.id.tvPetDetail)
        val tvPetWeight: TextView = view.findViewById(R.id.tvPetWeight)
        val tvPetVaccine: TextView = view.findViewById(R.id.tvPetVaccine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_card, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]

        holder.tvPetName.text = pet.name
        holder.tvPetDetail.text = "${pet.type} ${pet.breed}, ${pet.age}"
        holder.tvPetWeight.text = "${pet.weight} kg"

        // Load foto atau fallback icon
        if (pet.photoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(pet.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_pet_icon)
                .into(holder.ivPetPhoto)
            holder.ivPetPhoto.setOnClickListener {
                val ctx = holder.itemView.context
                val dialog = Dialog(ctx)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                val imageView = ImageView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setBackgroundColor(Color.BLACK)
                }
                Glide.with(ctx).load(pet.photoUrl).into(imageView)
                imageView.setOnClickListener { dialog.dismiss() }
                dialog.setContentView(imageView)
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                dialog.show()
            }
        } else {
            holder.ivPetPhoto.setImageResource(R.drawable.bg_pet_icon)
            holder.ivPetPhoto.setOnClickListener(null)
        }

        holder.tvPetVaccine.text = if (pet.allergy.isEmpty() || pet.allergy == "-") {
            "Sehat"
        } else {
            "Ada alergi"
        }

        holder.itemView.setOnClickListener { onPetClick(pet) }
    }

    override fun getItemCount() = pets.size

    fun updateList(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }
}