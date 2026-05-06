package com.nawaf.kasirpas.activity.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nawaf.kasirpas.MainActivity
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.adapter.CheckoutAdapter
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.FragmentCheckoutBinding
import com.nawaf.kasirpas.databinding.LayoutPaymentBottomSheetBinding
import com.nawaf.kasirpas.request.TransactionItemRequest
import com.nawaf.kasirpas.request.TransactionRequest
import com.nawaf.kasirpas.utils.PreferenceManager
import com.nawaf.kasirpas.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by activityViewModels()
    private lateinit var checkoutAdapter: CheckoutAdapter
    private lateinit var preferenceManager: PreferenceManager
    private var loadingDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        
        setupRecyclerView()
        observeViewModel()

        binding.btnProcessCheckout.setOnClickListener {
            if (viewModel.getCartItemCount() > 0) {
                showPaymentBottomSheet()
            } else {
                Toast.makeText(requireContext(), "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(message: String) {
        if (loadingDialog == null) {
            loadingDialog = Dialog(requireContext()).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(R.layout.layout_loading)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
        loadingDialog?.findViewById<TextView>(R.id.tvLoadingMessage)?.text = message
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun showPaymentBottomSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetBinding = LayoutPaymentBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val total = viewModel.getCartTotal()
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedTotal = format.format(total).replace("Rp", "Rp ")
        
        sheetBinding.tvTotalPaymentSheet.text = formattedTotal
        sheetBinding.btnConfirmPayment.text = "Pay $formattedTotal"

        // Handle Payment Selection
        sheetBinding.btnPayCash.setOnClickListener {
            sheetBinding.rbCash.isChecked = true
            sheetBinding.rbWallet.isChecked = false
            sheetBinding.rbBank.isChecked = false
        }
        sheetBinding.btnPayWallet.setOnClickListener {
            sheetBinding.rbCash.isChecked = false
            sheetBinding.rbWallet.isChecked = true
            sheetBinding.rbBank.isChecked = false
        }
        sheetBinding.btnPayBank.setOnClickListener {
            sheetBinding.rbCash.isChecked = false
            sheetBinding.rbWallet.isChecked = false
            sheetBinding.rbBank.isChecked = true
        }

        sheetBinding.btnConfirmPayment.setOnClickListener {
            val paymentMethod = when {
                sheetBinding.rbCash.isChecked -> "CASH"
                sheetBinding.rbWallet.isChecked -> "WALLET"
                sheetBinding.rbBank.isChecked -> "BANK"
                else -> "CASH"
            }
            performCheckout(paymentMethod, dialog)
        }

        dialog.show()
    }

    private fun performCheckout(paymentMethod: String, dialog: BottomSheetDialog) {
        val token = preferenceManager.getToken() ?: return
        val cartItems = viewModel.cartItems.value ?: return
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val itemsRequest = cartItems.map {
            TransactionItemRequest(
                productId = it.product.id,
                quantity = it.quantity,
                unitPrice = it.product.price.toDoubleOrNull() ?: 0.0
            )
        }

        val request = TransactionRequest(
            trxType = "SALE",
            trxDate = currentDate,
            paymentMethod = paymentMethod,
            items = itemsRequest
        )

        showLoading("Sedang memproses transaksi...")
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.transactionApi.storeTransaction("Bearer $token", request)
                hideLoading()
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Transaksi Berhasil!", Toast.LENGTH_SHORT).show()
                    viewModel.clearCart() 
                    dialog.dismiss()
                    
                    // Navigate back to Kasir and sync BottomNav
                    (activity as? MainActivity)?.setSelectedTab(R.id.nav_kasir)
                } else {
                    Toast.makeText(requireContext(), "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                hideLoading()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        checkoutAdapter = CheckoutAdapter(
            listOf(),
            onPlusClick = { item -> viewModel.addToCart(item.product) },
            onMinusClick = { item -> viewModel.removeFromCart(item.product) }
        )
        binding.rvCheckout.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = checkoutAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            val isEmpty = cartItems.isEmpty()
            
            binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvCheckout.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.cardSummary.visibility = if (isEmpty) View.GONE else View.VISIBLE
            
            checkoutAdapter.updateData(cartItems)
            
            val totalAmount = viewModel.getCartTotal()
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvTotalAmount.text = format.format(totalAmount).replace("Rp", "Rp ")
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        _binding = null
    }
}
