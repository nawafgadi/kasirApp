package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.databinding.ItemProductBinding
import com.nawaf.kasirpas.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>,
    private val onAddClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            
            val price = product.price.toDoubleOrNull() ?: 0.0
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvPrice.text = format.format(price).replace("Rp", "Rp ")
            
            val totalStock = product.stocks.sumOf { it.stock_on_hand }
            binding.tvStock.text = "Stok: $totalStock"

            binding.ivProduct.load(product.image_url) {
                crossfade(true)
                placeholder(R.drawable.ic_inventory)
            }

            binding.btnAddToCart.setOnClickListener {
                onAddClick(product)
            }
        }
    }
}
