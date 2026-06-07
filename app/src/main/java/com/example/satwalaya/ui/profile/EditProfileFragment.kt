package com.example.satwalaya.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.satwalaya.R
import com.example.satwalaya.databinding.FragmentEditProfileBinding
import com.example.satwalaya.ui.BaseFragment
import com.example.satwalaya.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : BaseFragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            Glide.with(this)
                .load(selectedImageUri)
                .circleCrop()
                .into(binding.ivEditAvatar)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Load foto yang sudah ada
        val photoUrl = sessionManager.getPhotoUrl()
        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_pet_icon)
                .into(binding.ivEditAvatar)
        }

        // Pre-fill fields
        binding.etEditName.setText(sessionManager.getUsername())
        binding.etEditEmail.setText(sessionManager.getEmail())
        binding.etEditPhone.setText(sessionManager.getPhone())

        // Pilih foto
        binding.tvChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etEditName.text.toString()
            val email = binding.etEditEmail.text.toString()
            val phone = binding.etEditPhone.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                if (selectedImageUri != null) {
                    uploadPhoto(name, email, phone)
                } else {
                    sessionManager.saveLoginSession(name, email, phone)
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } else {
                Toast.makeText(requireContext(), "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPhoto(name: String, email: String, phone: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profile_photos/$userId.jpg")

        binding.btnSaveProfile.isEnabled = false
        binding.btnSaveProfile.text = "Menyimpan..."

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    sessionManager.savePhotoUrl(uri.toString())
                    sessionManager.saveLoginSession(name, email, phone)
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener {
                binding.btnSaveProfile.isEnabled = true
                binding.btnSaveProfile.text = "Simpan Perubahan"
                Toast.makeText(requireContext(), "Gagal upload foto, coba lagi!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}