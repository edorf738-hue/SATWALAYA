package com.example.satwalaya.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.satwalaya.databinding.FragmentFaqBinding
import com.example.satwalaya.ui.BaseFragment

class FaqFragment : BaseFragment() {
    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}