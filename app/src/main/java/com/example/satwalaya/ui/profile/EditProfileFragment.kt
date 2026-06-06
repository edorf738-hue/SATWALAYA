package com.example.satwalaya.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Pre-fill fields from SessionManager
        binding.etEditName.setText(sessionManager.getUsername())
        binding.etEditEmail.setText(sessionManager.getEmail())
        binding.etEditPhone.setText(sessionManager.getPhone())

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etEditName.text.toString()
            val email = binding.etEditEmail.text.toString()
            val phone = binding.etEditPhone.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                // Simpan Sesi Pemilik
                sessionManager.saveLoginSession(name, email, phone)

                Toast.makeText(requireContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Nama dan Email wajib diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}