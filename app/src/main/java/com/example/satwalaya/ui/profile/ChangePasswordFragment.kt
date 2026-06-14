package com.example.satwalaya.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSavePassword.setOnClickListener {
            val oldPass = binding.etOldPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmPassword.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                Toast.makeText(requireContext(), "Konfirmasi kata sandi tidak sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass.length < 6) {
                Toast.makeText(requireContext(), "Kata sandi baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val email = user.email ?: return@setOnClickListener

            binding.btnSavePassword.isEnabled = false
            binding.btnSavePassword.text = "Menyimpan..."

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, oldPass)
                .addOnSuccessListener {
                    user.updatePassword(newPass)
                        .addOnSuccessListener {
                            if (_binding == null) return@addOnSuccessListener
                            Toast.makeText(requireContext(), "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener {
                            if (_binding == null) return@addOnFailureListener
                            binding.btnSavePassword.isEnabled = true
                            binding.btnSavePassword.text = "Simpan Perubahan"
                            Toast.makeText(requireContext(), "Gagal mengubah kata sandi", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    if (_binding == null) return@addOnFailureListener
                    binding.btnSavePassword.isEnabled = true
                    binding.btnSavePassword.text = "Simpan Perubahan"
                    Toast.makeText(requireContext(), "Kata sandi lama salah", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    if (_binding == null) return@addOnFailureListener
                    binding.btnSavePassword.isEnabled = true
                    binding.btnSavePassword.text = "Simpan Perubahan"
                    Toast.makeText(requireContext(), "Kata sandi lama salah", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
