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
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.nawaf.kasirpas.adapter.BluetoothDeviceAdapter
import com.nawaf.kasirpas.databinding.LayoutBluetoothPickerBinding
import com.nawaf.kasirpas.databinding.LayoutTransactionSuccessBinding
import com.nawaf.kasirpas.utils.ReceiptPrinterHelper
import com.nawaf.kasirpas.viewmodel.CartItem
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
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

    // Temp variables for receipt printing
    private var tempCartItems: List<CartItem> = listOf()
    private var tempTotal: Double = 0.0
    private var tempPaymentMethod: String = ""
    private var tempTransactionId: String = ""

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val connectGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions[android.Manifest.permission.BLUETOOTH_CONNECT] ?: false
        } else {
            permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        }
        
        if (connectGranted) {
            startPrintingFlow()
        } else {
            Toast.makeText(requireContext(), "Izin Bluetooth diperlukan untuk print struk", Toast.LENGTH_SHORT).show()
        }
    }


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
                    // Store details in temp variables for printing
                    tempCartItems = cartItems.toList()
                    tempTotal = viewModel.getCartTotal()
                    tempPaymentMethod = paymentMethod
                    
                    // Parse transaction ID if available
                    tempTransactionId = ""
                    try {
                        val bodyString = response.body()?.string()
                        if (!bodyString.isNullOrEmpty()) {
                            val jsonObject = org.json.JSONObject(bodyString)
                            if (jsonObject.has("data")) {
                                val dataObj = jsonObject.getJSONObject("data")
                                if (dataObj.has("id")) {
                                    tempTransactionId = dataObj.getInt("id").toString()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    dialog.dismiss()
                    showSuccessDialog()
                } else {
                    Toast.makeText(requireContext(), "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                hideLoading()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessDialog() {
        val successDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val successBinding = LayoutTransactionSuccessBinding.inflate(layoutInflater)
        successDialog.setContentView(successBinding.root)
        successDialog.setCancelable(false)

        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedTotal = format.format(tempTotal).replace("Rp", "Rp ")
        
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())

        successBinding.tvTransactionTotal.text = formattedTotal
        successBinding.tvTransactionDate.text = dateStr
        successBinding.tvPaymentMethod.text = tempPaymentMethod

        successBinding.btnPrintReceipt.setOnClickListener {
            if (checkBluetoothPermissions()) {
                startPrintingFlow()
            } else {
                requestBluetoothPermissions()
            }
        }

        successBinding.btnFinish.setOnClickListener {
            viewModel.clearCart()
            successDialog.dismiss()
            (activity as? MainActivity)?.setSelectedTab(R.id.nav_kasir)
        }

        successDialog.show()
    }

    private fun checkBluetoothPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            requestMultiplePermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            requestMultiplePermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun startPrintingFlow() {
        val savedAddress = preferenceManager.getPrinterAddress()
        if (savedAddress != null) {
            val printers = ReceiptPrinterHelper.getPairedPrinters()
            val savedPrinter = printers.find { it.device.address == savedAddress }
            if (savedPrinter != null) {
                printToPrinter(savedPrinter)
                return
            }
        }
        showBluetoothPicker()
    }

    private fun showBluetoothPicker() {
        val pickerDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val pickerBinding = LayoutBluetoothPickerBinding.inflate(layoutInflater)
        pickerDialog.setContentView(pickerBinding.root)

        val devices = ReceiptPrinterHelper.getPairedPrinters()
        val adapter = BluetoothDeviceAdapter(devices) { selectedConnection ->
            preferenceManager.savePrinterAddress(selectedConnection.device.address)
            printToPrinter(selectedConnection)
            pickerDialog.dismiss()
        }

        pickerBinding.rvBluetoothDevices.layoutManager = LinearLayoutManager(requireContext())
        pickerBinding.rvBluetoothDevices.adapter = adapter

        val updateViews = {
            val updatedDevices = ReceiptPrinterHelper.getPairedPrinters()
            adapter.updateData(updatedDevices)
            if (updatedDevices.isEmpty()) {
                pickerBinding.layoutEmptyBluetooth.visibility = View.VISIBLE
                pickerBinding.rvBluetoothDevices.visibility = View.GONE
            } else {
                pickerBinding.layoutEmptyBluetooth.visibility = View.GONE
                pickerBinding.rvBluetoothDevices.visibility = View.VISIBLE
            }
        }

        updateViews()

        pickerBinding.btnRefreshBluetooth.setOnClickListener {
            updateViews()
        }

        pickerDialog.show()
    }

    private fun printToPrinter(connection: BluetoothConnection) {
        val cashierName = preferenceManager.getUser()?.name ?: "Kasir"
        showLoading("Sedang mencetak struk...")
        lifecycleScope.launch {
            val success = ReceiptPrinterHelper.printReceipt(
                requireContext(),
                connection,
                tempTransactionId,
                tempCartItems,
                tempTotal,
                tempPaymentMethod,
                cashierName
            )
            hideLoading()
            if (success) {
                Toast.makeText(requireContext(), "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Gagal mencetak struk. Pastikan printer menyala dan terhubung.", Toast.LENGTH_LONG).show()
                showBluetoothPicker()
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
