package com.example.satwalaya.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.satwalaya.databinding.FragmentHelpSupportBinding
import com.example.satwalaya.ui.BaseFragment

class HelpSupportFragment : BaseFragment() {
    private var _binding: FragmentHelpSupportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHelpSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContactWhatsapp.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=6281234567890&text=Halo%20Satwalaya,%20saya%20butuh%20bantuan."
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.btnContactEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:support@satwalaya.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Customer Support Request")
            startActivity(intent)
        }

        binding.btnFaq.setOnClickListener {
            Toast.makeText(requireContext(), "FAQ feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}