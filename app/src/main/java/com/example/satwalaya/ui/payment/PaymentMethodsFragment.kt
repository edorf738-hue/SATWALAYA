package com.example.satwalaya.ui.payment

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.databinding.FragmentPaymentMethodsBinding

class PaymentMethodsFragment : Fragment() {
    private var _binding: FragmentPaymentMethodsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        updateUI()

        binding.btnAddPayment.setOnClickListener {
            showAddPaymentDialog()
        }
    }

    private fun updateUI() {
        val savedCard = sessionManager.getSavedCard()
        if (savedCard.isNotEmpty()) {
            binding.layoutSavedCard.visibility = View.VISIBLE
            binding.dividerCard.visibility = View.VISIBLE
            binding.tvNoCard.visibility = View.GONE
            binding.tvCardName.text = "Card •••• ${savedCard.takeLast(4)}"
        } else {
            binding.layoutSavedCard.visibility = View.GONE
            binding.dividerCard.visibility = View.GONE
            binding.tvNoCard.visibility = View.VISIBLE
        }
    }

    private fun showAddPaymentDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        val etCardNumber = EditText(requireContext()).apply {
            hint = "16 Digit Card Number"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(etCardNumber)

        AlertDialog.Builder(requireContext())
            .setTitle("Manage Card")
            .setMessage("Enter your credit/debit card number to save it for future bookings.")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val card = etCardNumber.text.toString()
                if (card.length == 16) {
                    sessionManager.saveCard(card)
                    updateUI()
                    Toast.makeText(requireContext(), "Payment method updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid 16-digit card number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}