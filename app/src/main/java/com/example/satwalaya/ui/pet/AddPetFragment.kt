package com.example.satwalaya.ui.pet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.data.model.Pet
import com.example.satwalaya.data.repository.PetRepository
import com.example.satwalaya.databinding.FragmentAddPetBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class AddPetFragment : Fragment() {
    private var _binding: FragmentAddPetBinding? = null
    private val binding get() = _binding!!
    private val repository = PetRepository()
    private var selectedType = "Kucing"
    private var editPetId = ""
    private var selectedPhotoUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = result.data?.data
            selectedPhotoUri?.let {
                Glide.with(this).load(it).circleCrop().into(binding.ivPetPhoto)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = cameraImageUri
            selectedPhotoUri?.let {
                Glide.with(this).load(it).circleCrop().into(binding.ivPetPhoto)
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(requireContext(), "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            editPetId = args.getString("petId", "")
            if (editPetId.isNotEmpty()) {
                binding.tvTitle.text = "Edit hewan"
                binding.etPetName.setText(args.getString("petName", ""))
                binding.etPetBreed.setText(args.getString("petBreed", ""))
                binding.etPetAge.setText(args.getString("petAge", ""))
                binding.etPetWeight.setText(args.getString("petWeight", ""))
                binding.etPetAllergy.setText(args.getString("petAllergy", ""))
                binding.etFeedSchedule.setText(args.getString("petFeedSchedule", ""))
                binding.etFeedType.setText(args.getString("petFeedType", ""))
                selectType(args.getString("petType", "Kucing"))

                val existingPhoto = args.getString("petPhotoUrl", "")
                if (existingPhoto.isNotEmpty()) {
                    Glide.with(this).load(existingPhoto).circleCrop().into(binding.ivPetPhoto)
                }
            }
        }

        binding.chipKucing.setOnClickListener { selectType("Kucing") }
        binding.chipAnjing.setOnClickListener { selectType("Anjing") }
        binding.chipKelinci.setOnClickListener { selectType("Kelinci") }
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { savePet() }
        binding.btnCamera.setOnClickListener { checkCameraPermission() }
        binding.btnGallery.setOnClickListener { openGallery() }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("pet_photo_", ".jpg", requireContext().cacheDir)
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun selectType(type: String) {
        selectedType = type
        val chips = listOf(
            binding.chipKucing to "Kucing",
            binding.chipAnjing to "Anjing",
            binding.chipKelinci to "Kelinci"
        )
        chips.forEach { (chip, chipType) ->
            if (chipType == type) {
                chip.setBackgroundResource(R.drawable.bg_pet_icon)
                chip.setTextColor(resources.getColor(R.color.purple_primary, null))
            } else {
                chip.setBackgroundResource(R.drawable.bg_input_field)
                chip.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }
    }

    private fun savePet() {
        val name = binding.etPetName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Nama hewan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val pet = Pet(
            id = editPetId,
            userId = userId,
            name = name,
            type = selectedType,
            breed = binding.etPetBreed.text.toString().trim(),
            age = binding.etPetAge.text.toString().trim(),
            weight = binding.etPetWeight.text.toString().trim(),
            allergy = binding.etPetAllergy.text.toString().trim(),
            feedSchedule = binding.etFeedSchedule.text.toString().trim(),
            feedType = binding.etFeedType.text.toString().trim()
        )

        repository.savePetWithPhoto(pet, selectedPhotoUri,
            onSuccess = {
                Toast.makeText(requireContext(), "Data hewan berhasil disimpan", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            },
            onFailure = {
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}