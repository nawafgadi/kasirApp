package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.databinding.ItemCheckoutBinding
import com.nawaf.kasirpas.viewmodel.CartItem
import java.text.NumberFormat
import java.util.Locale

class CheckoutAdapter(
    private var cartItems: List<CartItem>,
    private val onPlusClick: (CartItem) -> Unit,
    private val onMinusClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    fun updateData(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val binding = ItemCheckoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CheckoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CheckoutViewHolder(private val binding: ItemCheckoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvProductName.text = item.product.name
            
            val price = item.product.price.toDoubleOrNull() ?: 0.0
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvPrice.text = format.format(price).replace("Rp", "Rp ")
            
            binding.tvQuantity.text = item.quantity.toString()

            binding.ivProduct.load(item.product.image_url) {
                crossfade(true)
                placeholder(R.drawable.ic_inventory)
            }

            binding.btnPlus.setOnClickListener { onPlusClick(item) }
            binding.btnMinus.setOnClickListener { onMinusClick(item) }
        }
    }
}
