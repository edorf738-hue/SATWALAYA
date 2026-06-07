package com.example.satwalaya.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentNotificationsBinding
import com.example.satwalaya.ui.BaseFragment


class NotificationsFragment : BaseFragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Load saved states
        binding.switchBooking.isChecked = sessionManager.getBookingNotif()
        binding.switchPromo.isChecked = sessionManager.getPromoNotif()
        binding.switchHealth.isChecked = sessionManager.getHealthNotif()

        // Save on change
        binding.switchBooking.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setBookingNotif(isChecked)
            val status = if (isChecked) "diaktifkan" else "dimatikan"
            Toast.makeText(requireContext(), "Pembaruan booking $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchPromo.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setPromoNotif(isChecked)
            val status = if (isChecked) "diaktifkan" else "dimatikan"
            Toast.makeText(requireContext(), "Promosi $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchHealth.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setHealthNotif(isChecked)
            val status = if (isChecked) "diaktifkan" else "dimatikan"
            Toast.makeText(requireContext(), "Tips kesehatan $status", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}