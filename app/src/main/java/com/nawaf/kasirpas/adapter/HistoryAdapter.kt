package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nawaf.kasirpas.databinding.ItemTransactionHistoryBinding
import com.nawaf.kasirpas.response.TransactionHistory
import java.text.NumberFormat
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var items: MutableList<TransactionHistory> = mutableListOf()

    fun setItems(newItems: List<TransactionHistory>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<TransactionHistory>) {
        val startPos = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPos, newItems.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemTransactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionHistory) {
            binding.tvTrxId.text = "#${transaction.id}"
            binding.tvTrxType.text = transaction.trxType
            binding.tvTrxDate.text = transaction.trxDate
            binding.tvUserName.text = transaction.user.name
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.tvTotalAmount.text = formatter.format(transaction.totalAmount)
                .replace("Rp", "Rp ")
                .replace(",00", "")

            val itemsSummary = transaction.items.joinToString("\n") { item ->
                "${item.quantity}x ${item.product.name} @ ${formatter.format(item.product.price).replace("Rp", "Rp ").replace(",00", "")}"
            }
            binding.tvItemsSummary.text = itemsSummary
        }
    }
}
