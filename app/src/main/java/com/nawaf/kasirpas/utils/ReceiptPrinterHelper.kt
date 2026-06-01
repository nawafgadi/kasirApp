package com.nawaf.kasirpas.utils

import android.content.Context
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.nawaf.kasirpas.viewmodel.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPrinterHelper {

    fun getPairedPrinters(): List<BluetoothConnection> {
        return try {
            val connections = BluetoothPrintersConnections().getList()
            connections?.toList() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun printReceipt(
        context: Context,
        connection: BluetoothConnection,
        transactionId: String,
        cartItems: List<CartItem>,
        totalAmount: Double,
        paymentMethod: String,
        cashierName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if connection is valid
            val printer = EscPosPrinter(connection, 203, 48f, 32)
            
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formattedTotal = format.format(totalAmount).replace("Rp", "Rp ")
            
            val receiptNum = if (transactionId.isNotEmpty()) transactionId else "TRX-${System.currentTimeMillis() % 100000}"

            var itemsText = ""
            for (item in cartItems) {
                val name = item.product.name
                val qty = item.quantity
                val priceDouble = item.product.price.toDoubleOrNull() ?: 0.0
                val subtotal = priceDouble * qty
                val formattedSubtotal = format.format(subtotal).replace("Rp", "").trim()
                
                // If product name is long, print it on its own line first
                if (name.length > 18) {
                    itemsText += "[L]${name}\n"
                    itemsText += "[L]  x${qty}[R]${formattedSubtotal}\n"
                } else {
                    itemsText += "[L]${name}[R]x${qty}   ${formattedSubtotal}\n"
                }
            }

            val formattedText = """
                [C]<b><font size='big'>LuxePOS</font></b>
                [C]================================
                [L]No. Resi : $receiptNum
                [L]Tanggal  : $dateStr
                [L]Kasir    : $cashierName
                [L]Bayar    : $paymentMethod
                [C]--------------------------------
                [L]<b>Menu</b>[R]<b>Qty   Harga</b>
                [C]--------------------------------
                $itemsText
                [C]--------------------------------
                [L]<b>TOTAL</b>[R]<b>$formattedTotal</b>
                [C]================================
                [C]Terima Kasih!
                [C]Semoga Berkah 🙏
                [C]
                [C]
            """.trimIndent()

            printer.printFormattedText(formattedText)
            printer.disconnectPrinter()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
