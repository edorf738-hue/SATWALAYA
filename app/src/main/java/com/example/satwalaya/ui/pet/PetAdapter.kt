package com.example.satwalaya.ui.pet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.satwalaya.R
import com.example.satwalaya.data.model.Pet

class PetAdapter(
    private var pets: List<Pet>,
    private val onPetClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPetIcon: TextView = view.findViewById(R.id.tvPetIcon)
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

        holder.tvPetIcon.text = when (pet.type) {
            "Kucing" -> "🐱"
            "Anjing" -> "🐶"
            "Kelinci" -> "🐰"
            else -> "🐾"
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