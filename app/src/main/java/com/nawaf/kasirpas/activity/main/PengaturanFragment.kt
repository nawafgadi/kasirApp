package com.nawaf.kasirpas.activity.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.activity.KelolaKategoriActivity
import com.nawaf.kasirpas.activity.KelolaProductActivity
import com.nawaf.kasirpas.activity.LoginActivity
import com.nawaf.kasirpas.databinding.FragmentPengaturanBinding
import com.nawaf.kasirpas.utils.PreferenceManager

class PengaturanFragment : Fragment() {

    private var _binding: FragmentPengaturanBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengaturanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        setupUserUI()
        setupListeners()
    }

    private fun setupUserUI() {
        val user = prefManager.getUser()
        if (user != null) {
            binding.tvUserName.text = user.name
            binding.tvUserRole.text = "Store Manager • ${user.email}"

            binding.ivProfilePicture.load("https://img.freepik.com/free-vector/businessman-character-avatar-isolated_24877-60111.jpg") {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }
        }
    }

    private fun setupListeners() {
        // Klik Kelola Kategori sekarang beneran pindah halaman
        binding.btnManageCategories.setOnClickListener {
            val intent = Intent(requireContext(), KelolaKategoriActivity::class.java)
            startActivity(intent)
        }

        // Klik Kelola Produk sekarang beneran pindah halaman
        binding.btnManageProducts.setOnClickListener {
            val intent = Intent(requireContext(), KelolaProductActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        prefManager.clear()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}