package com.example.satwalaya.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentPrivacySecurityBinding

class PrivacySecurityFragment : Fragment() {
    private var _binding: FragmentPrivacySecurityBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPrivacySecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Muat status yang tersimpan
        binding.switchTwoFactor.isChecked = sessionManager.is2FAEnabled()
        binding.switchBiometric.isChecked = sessionManager.isBiometricEnabled()

        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur ubah kata sandi akan segera hadir!", Toast.LENGTH_SHORT).show()
        }

        binding.switchTwoFactor.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.set2FA(isChecked)
            val status = if (isChecked) "diaktifkan" else "dimatikan"
            Toast.makeText(requireContext(), "Autentikasi dua faktor $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setBiometric(isChecked)
            val status = if (isChecked) "diaktifkan" else "dimatikan"
            Toast.makeText(requireContext(), "Login biometrik $status", Toast.LENGTH_SHORT).show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Permintaan penghapusan akun telah dikirim ke admin", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}