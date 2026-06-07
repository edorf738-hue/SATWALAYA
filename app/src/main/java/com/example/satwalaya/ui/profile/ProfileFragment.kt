package com.example.satwalaya.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.R
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentProfileBinding
import com.example.satwalaya.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        updateUI()

        binding.menuEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_editProfileFragment)
        }
        binding.menuMyPets.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_petListFragment)
        }
        binding.menuNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_notificationsFragment)
        }
        binding.menuChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        binding.menuHelp.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_helpSupportFragment)
        }
        binding.menuAbout.setOnClickListener {
            showAboutDialog()
        }
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun updateUI() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvProfileName.text = user?.displayName?.ifEmpty { sessionManager.getUsername() }
            ?: sessionManager.getUsername().ifEmpty { "Pet Owner" }
        binding.tvProfileEmail.text = user?.email
            ?: sessionManager.getEmail().ifEmpty { "user@satwalaya.com" }

        // Load foto profil
        val photoUrl = sessionManager.getPhotoUrl()
        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_pet_icon)
                .into(binding.ivProfileAvatar)
        }
    }
    private fun showChangePasswordDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Kata Sandi")
            .setMessage("Link ubah kata sandi akan dikirim ke email kamu.")
            .setPositiveButton("Kirim") { _, _ ->
                val email = FirebaseAuth.getInstance().currentUser?.email ?: return@setPositiveButton
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                android.widget.Toast.makeText(requireContext(), "Link dikirim ke $email", android.widget.Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang Satwalaya")
            .setMessage("Satwalaya v1.0.0\n\nAplikasi pet hotel dan grooming terpercaya untuk anabul kesayangan kamu.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Keluar Akun")
            .setMessage("Yakin ingin keluar dari akun kamu?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                sessionManager.clearSession()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}