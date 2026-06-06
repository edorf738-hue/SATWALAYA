package com.example.satwalaya.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.tvLanguageValue.text = "Indonesia"
        binding.switchDarkMode.isChecked = sessionManager.isDarkMode()

        binding.btnLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Bahasa aktif: Indonesia", Toast.LENGTH_SHORT).show()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.btnAbout.setOnClickListener {
            Toast.makeText(requireContext(), "Satwalaya v1.0.1 - Dibuat dengan ❤️", Toast.LENGTH_LONG).show()
        }

        binding.btnResetData.setOnClickListener {
            showResetConfirmation()
        }
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Data Aplikasi")
            .setMessage("Untuk menghapus data booking, silakan hubungi admin Satwalaya.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}