package com.example.satwalaya.ui.pet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.satwalaya.R
import com.example.satwalaya.data.repository.PetRepository
import com.example.satwalaya.databinding.FragmentPetListBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.satwalaya.ui.BaseFragment

class PetListFragment : BaseFragment() {
    private var _binding: FragmentPetListBinding? = null
    private val binding get() = _binding!!
    private val repository = PetRepository()
    private lateinit var adapter: PetAdapter

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
    }

    override fun onResume() {
        super.onResume()
        loadPets()
    }

    private fun loadPets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        repository.getPets(userId,
            onSuccess = { pets ->
                if (pets.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvPetList.visibility = View.GONE
                    binding.bottomAddButton.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvPetList.visibility = View.VISIBLE
                    binding.bottomAddButton.visibility = View.VISIBLE
                    adapter.updateList(pets)
                }
            },
            onFailure = {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvPetList.visibility = View.GONE
                binding.bottomAddButton.visibility = View.GONE
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}