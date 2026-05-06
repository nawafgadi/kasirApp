package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.databinding.ItemManageProductBinding
import com.nawaf.kasirpas.model.Product
import java.text.NumberFormat
import java.util.*

class ManageProductAdapter(
    private var products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ManageProductAdapter.ViewHolder>() {

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManageProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(private val binding: ItemManageProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductCategory.text = product.category?.name ?: "Tanpa Kategori"
            
            val price = product.price.toDoubleOrNull() ?: 0.0
            binding.tvProductPrice.text = formatRupiah(price)
            
            val totalStock = product.stocks?.sumOf { it.stockOnHand ?: 0 } ?: 0
            binding.tvProductStock.text = "Stok: $totalStock"

            val url = product.imageUrl
            if (!url.isNullOrEmpty()) {
                binding.ivProduct.load(url) {
                    crossfade(true)
                    placeholder(R.drawable.ic_inventory)
                    error(R.drawable.ic_inventory)
                }
            } else {
                binding.ivProduct.setImageResource(R.drawable.ic_inventory)
            }

            binding.btnEdit.setOnClickListener { onEditClick(product) }
            binding.btnHapus.setOnClickListener { onDeleteClick(product) }
        }

        private fun formatRupiah(number: Double): String {
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            return formatRupiah.format(number).replace("Rp", "Rp ")
        }
    }
}
