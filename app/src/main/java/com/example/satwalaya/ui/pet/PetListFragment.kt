package com.example.satwalaya.ui.pet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.satwalaya.R
import com.example.satwalaya.data.model.Pet
import com.example.satwalaya.databinding.FragmentPetListBinding
import com.example.satwalaya.ui.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PetListFragment : BaseFragment() {
    private var _binding: FragmentPetListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PetAdapter
    private var petsListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPetListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PetAdapter(emptyList()) { pet ->
            val bundle = Bundle().apply {
                putString("petId", pet.id)
                putString("petName", pet.name)
                putString("petType", pet.type)
                putString("petBreed", pet.breed)
                putString("petAge", pet.age)
                putString("petWeight", pet.weight)
                putString("petAllergy", pet.allergy)
                putString("petFeedSchedule", pet.feedSchedule)
                putString("petFeedType", pet.feedType)
                putString("petPhotoUrl", pet.photoUrl)
            }
            findNavController().navigate(R.id.action_petList_to_addPet, bundle)
        }

        binding.rvPetList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPetList.adapter = adapter

        binding.btnAddPet.setOnClickListener {
            findNavController().navigate(R.id.action_petList_to_addPet)
        }

        binding.btnAddPetEmpty.setOnClickListener {
            findNavController().navigate(R.id.action_petList_to_addPet)
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        petsListener = FirebaseFirestore.getInstance()
            .collection("pets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { result, _ ->
                if (_binding == null) return@addSnapshotListener
                if (result == null || result.isEmpty) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvPetList.visibility = View.GONE
                    binding.bottomAddButton.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvPetList.visibility = View.VISIBLE
                    binding.bottomAddButton.visibility = View.VISIBLE
                    val pets = result.documents.map { doc ->
                        Pet(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            name = doc.getString("name") ?: "",
                            type = doc.getString("type") ?: "",
                            breed = doc.getString("breed") ?: "",
                            age = doc.getString("age") ?: "",
                            weight = doc.getString("weight") ?: "",
                            allergy = doc.getString("allergy") ?: "",
                            feedSchedule = doc.getString("feedSchedule") ?: "",
                            feedType = doc.getString("feedType") ?: "",
                            photoUrl = doc.getString("photoUrl") ?: ""
                        )
                    }
                    adapter.updateList(pets)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        petsListener?.remove()
        petsListener = null
        _binding = null
    }
}