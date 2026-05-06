package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nawaf.kasirpas.databinding.ItemManageCategoryBinding
import com.nawaf.kasirpas.model.Category

class ManageCategoryAdapter(
    private var categories: List<Category>,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit,
    private val onStatusChange: (Category, Boolean) -> Unit
) : RecyclerView.Adapter<ManageCategoryAdapter.ViewHolder>() {

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManageCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(private val binding: ItemManageCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            
            val isActive = category.isActive == 1
            
            // Clear listener to avoid triggering it when binding
            binding.switchStatus.setOnCheckedChangeListener(null)
            binding.switchStatus.isChecked = isActive
            
            updateStatusUI(isActive)

            binding.btnEdit.setOnClickListener { onEditClick(category) }
            binding.btnHapus.setOnClickListener { onDeleteClick(category) }
            
            binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
                onStatusChange(category, isChecked)
                updateStatusUI(isChecked)
            }
        }

        private fun updateStatusUI(isActive: Boolean) {
            if (isActive) {
                binding.tvStatus.text = "Terlihat"
                binding.tvStatus.setTextColor(0xFF10B981.toInt())
            } else {
                binding.tvStatus.text = "Tersembunyi"
                binding.tvStatus.setTextColor(0xFF6B7280.toInt())
            }
        }
    }
}
