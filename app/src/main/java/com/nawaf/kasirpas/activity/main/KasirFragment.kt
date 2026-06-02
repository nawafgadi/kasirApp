package com.nawaf.kasirpas.activity.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.nawaf.kasirpas.MainActivity
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.adapter.CategoryAdapter
import com.nawaf.kasirpas.adapter.ProductAdapter
import com.nawaf.kasirpas.databinding.FragmentKasirBinding
import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.utils.PreferenceManager
import com.nawaf.kasirpas.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

class KasirFragment : Fragment() {

    private var _binding: FragmentKasirBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()

    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var prefManager: PreferenceManager

    private var currentQuery: String = ""
    private var currentCategoryId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKasirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        setupRecyclerViews()
        setupSearch()
        observeViewModel()

        val token = prefManager.getToken() ?: ""
        viewModel.loadProducts(token)

        // Handle Navigation to Checkout via Floating Bar
        binding.cardCheckoutBar.setOnClickListener {
            // Use MainActivity's method to sync bottom navigation state
            (activity as? MainActivity)?.setSelectedTab(R.id.nav_checkout)
        }
    }

    fun refreshData() {
        val token = prefManager.getToken() ?: ""
        viewModel.loadProducts(token, forceRefresh = true)
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            applyFilters()
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val activeCategories = categories.filter { it.isActive == 1 }
            categoryAdapter.updateData(activeCategories)
            applyFilters()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Use MainActivity's SwipeRefresh indicator for loading states to avoid double spinners
            (activity as? MainActivity)?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)?.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)?.isRefreshing = false
            }
        }

        // Observe Toast Messages (Stock validation, etc)
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        }

        // Observe Cart Items to show/hide and update Floating Bar
        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isNotEmpty()) {
                binding.cardCheckoutBar.visibility = View.VISIBLE

                val totalCount = viewModel.getCartItemCount()
                val totalPrice = viewModel.getCartTotal()

                binding.tvCheckoutItemCount.text = "$totalCount Items"
                binding.tvCartBadge.text = totalCount.toString()

                val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                binding.tvCheckoutTotalPrice.text = format.format(totalPrice).replace("Rp", "Rp ")
            } else {
                binding.cardCheckoutBar.visibility = View.GONE
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilters() {
        val filtered = viewModel.getFilteredProducts(currentQuery, currentCategoryId)
        productAdapter.updateData(filtered)
    }

    private fun setupRecyclerViews() {
        productAdapter = ProductAdapter(
            products = listOf(),
            onImageClick = { imageUrl ->
                showImageDialog(imageUrl)
            },
            onAddClick = { product ->
                viewModel.addToCart(product)
            }
        )
        binding.rvProducts.adapter = productAdapter

        categoryAdapter = CategoryAdapter(listOf()) { category ->
            currentCategoryId = category?.id
            applyFilters()
        }
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun showImageDialog(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) return

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_image_preview)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val imageView = dialog.findViewById<ImageView>(R.id.ivPreview)
        val btnClose = dialog.findViewById<ImageView>(R.id.btnClosePreview)

        imageView.load(imageUrl) {
            crossfade(true)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        imageView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
