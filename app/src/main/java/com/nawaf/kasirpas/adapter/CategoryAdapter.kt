package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.databinding.ItemCategoryBinding
import com.nawaf.kasirpas.model.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category?) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedCategoryId: Int? = null // null means "Semua"

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // Position 0 is always "Semua"
        if (position == 0) {
            holder.bind(null)
        } else {
            holder.bind(categories[position - 1])
        }
    }

    override fun getItemCount(): Int = categories.size + 1

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category?) {
            val isSelected = if (category == null) {
                selectedCategoryId == null
            } else {
                selectedCategoryId == category.id
            }

            binding.tvCategoryName.text = category?.name ?: "Semua"

            if (isSelected) {
                binding.cardCategory.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.primary_container)
                )
                binding.tvCategoryName.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.on_primary)
                )
                binding.cardCategory.strokeWidth = 0
            } else {
                binding.cardCategory.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.white)
                )
                binding.tvCategoryName.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.on_surface_variant)
                )
                binding.cardCategory.strokeWidth = 1
            }

            binding.root.setOnClickListener {
                selectedCategoryId = category?.id
                onCategoryClick(category)
                notifyDataSetChanged()
            }
        }
    }
}
