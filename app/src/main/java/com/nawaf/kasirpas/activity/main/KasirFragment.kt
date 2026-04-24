package com.nawaf.kasirpas.activity.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nawaf.kasirpas.activity.OnboardingActivity
import com.nawaf.kasirpas.databinding.FragmentKasirBinding
import com.nawaf.kasirpas.utils.PreferenceManager

class KasirFragment : Fragment() {

    private var _binding: FragmentKasirBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKasirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTestOnboarding.setOnClickListener {
            // Reset status onboarding agar MainActivity mengizinkan masuk ke OnboardingActivity
            val prefManager = PreferenceManager(requireContext())
            prefManager.setOnboarded(false)
            
            val intent = Intent(requireContext(), OnboardingActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
