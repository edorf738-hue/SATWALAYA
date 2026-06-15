package com.example.satwalaya.ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.satwalaya.R
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentProfileBinding
import com.example.satwalaya.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var profileListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        val user = FirebaseAuth.getInstance().currentUser
        val cachedName = sessionManager.getUsername().ifEmpty { user?.displayName?.ifEmpty { "Pet Owner" } ?: "Pet Owner" }
        val cachedEmail = sessionManager.getEmail().ifEmpty { user?.email ?: "user@satwalaya.com" }
        bindProfileData(cachedName, cachedEmail, sessionManager.getPhotoUrl())

        val uid = user?.uid
        if (uid != null) {
            profileListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .addSnapshotListener { doc, _ ->
                    if (!isAdded || _binding == null || doc == null) return@addSnapshotListener
                    val name = doc.getString("name")?.ifEmpty { null } ?: sessionManager.getUsername()
                    val email = doc.getString("email")?.ifEmpty { null } ?: sessionManager.getEmail()
                    val phone = doc.getString("phone") ?: sessionManager.getPhone()
                    val photoUrl = doc.getString("photoUrl") ?: sessionManager.getPhotoUrl()
                    bindProfileData(name, email, photoUrl)
                    sessionManager.saveLoginSession(name, email, phone)
                    if (photoUrl.isNotEmpty()) sessionManager.savePhotoUrl(photoUrl)
                }
        }

        binding.menuEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_editProfileFragment)
        }
        binding.menuMyPets.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_petListFragment)
        }
        binding.menuNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_notificationSettingsFragment)
        }
        val isGoogleUser = FirebaseAuth.getInstance().currentUser?.providerData
            ?.any { it.providerId == "google.com" } ?: false
        binding.menuChangePassword.visibility = if (isGoogleUser) View.GONE else View.VISIBLE
        binding.dividerAbout.visibility = if (isGoogleUser) View.GONE else View.VISIBLE
        binding.menuChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_changePasswordFragment)
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

    private fun bindProfileData(name: String, email: String, photoUrl: String) {
        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.bg_pet_icon)
                .into(binding.ivProfileAvatar)
            binding.ivProfileAvatar.setOnClickListener { showFullscreenPhoto(photoUrl) }
        } else {
            binding.ivProfileAvatar.setImageResource(R.drawable.bg_pet_icon)
            binding.ivProfileAvatar.setOnClickListener(null)
        }
    }

    private fun showFullscreenPhoto(url: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }
        Glide.with(requireContext()).load(url).into(imageView)
        imageView.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(imageView)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.show()
    }
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang Satwalaya")
            .setMessage(
                "Satwalaya v1.0.0\n\n" +
                        "Aplikasi pet hotel dan grooming terpercaya untuk anabul kesayangan kamu.\n\n" +
                        "Dikembangkan oleh Tim Satwalaya\n" +
                        "© 2026 Satwalaya. All rights reserved."
            )
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

    override fun onDestroyView() {
        super.onDestroyView()
        profileListener?.remove()
        profileListener = null
        _binding = null
    }
}