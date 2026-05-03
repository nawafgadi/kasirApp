package com.nawaf.kasirpas.activity.main

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.activity.AiBussyHoursActivity
import com.nawaf.kasirpas.activity.AiStocksActivity
import com.nawaf.kasirpas.activity.LoginActivity
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.FragmentPengaturanBinding
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch

class PengaturanFragment : Fragment() {

    private var _binding: FragmentPengaturanBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PreferenceManager
    private var isProMax = false

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

    override fun onResume() {
        super.onResume()
        checkSubscriptionStatus()
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

    private fun checkSubscriptionStatus() {
        val token = prefManager.getToken() ?: return
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.billingApi.getActiveSubscription("Bearer $token")
                if (response.isSuccessful) {
                    val activeSub = response.body()?.data
                    isProMax = activeSub != null && activeSub.status == "ACTIVE" && activeSub.planName == "PRO_MAX"
                    updateAiFeaturesUI()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateAiFeaturesUI() {
        if (!isAdded) return
        val context = requireContext()
        
        if (isProMax) {
            // Unlocked State - Premium Theme (Tanpa Border)
            binding.ivLockStock.setImageResource(R.drawable.ic_chevron_right)
            binding.ivLockBusyHours.setImageResource(R.drawable.ic_chevron_right)
            binding.ivLockStock.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline_variant))
            binding.ivLockBusyHours.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline_variant))

            // Warna Gold untuk Icon
            val goldColor = ContextCompat.getColor(context, R.color.premium_gold)
            binding.containerIconStock.setCardBackgroundColor(ColorStateList.valueOf(goldColor).withAlpha(40))
            binding.containerIconBusyHours.setCardBackgroundColor(ColorStateList.valueOf(goldColor).withAlpha(40))
            
            binding.ivIconStock.imageTintList = ColorStateList.valueOf(goldColor)
            binding.ivIconBusyHours.imageTintList = ColorStateList.valueOf(goldColor)
            
            binding.btnAiStock.alpha = 1.0f
            binding.btnAiBusyHours.alpha = 1.0f
        } else {
            // Locked State - Tampilan Samar (Faint) & Tanpa Border
            binding.ivLockStock.setImageResource(R.drawable.ic_lock)
            binding.ivLockBusyHours.setImageResource(R.drawable.ic_lock)
            binding.ivLockStock.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline))
            binding.ivLockBusyHours.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline))

            // Icon Abu-abu
            val greyColor = ContextCompat.getColor(context, R.color.outline)
            binding.containerIconStock.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_variant))
            binding.containerIconBusyHours.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_variant))
            
            binding.ivIconStock.imageTintList = ColorStateList.valueOf(greyColor)
            binding.ivIconBusyHours.imageTintList = ColorStateList.valueOf(greyColor)
            
            // Efek Samar
            binding.btnAiStock.alpha = 0.4f
            binding.btnAiBusyHours.alpha = 0.4f
        }
    }

    private fun setupListeners() {
        binding.btnAiStock.setOnClickListener {
            if (isProMax) {
                startActivity(Intent(requireContext(), AiStocksActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "🔒 Fitur ini hanya untuk member PROMAX", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAiBusyHours.setOnClickListener {
            if (isProMax) {
                startActivity(Intent(requireContext(), AiBussyHoursActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "🔒 Fitur ini hanya untuk member PROMAX", Toast.LENGTH_SHORT).show()
            }
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
