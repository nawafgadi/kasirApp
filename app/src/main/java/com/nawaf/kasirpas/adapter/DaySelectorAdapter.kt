package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.databinding.ItemDaySelectorBinding
import com.nawaf.kasirpas.model.DailyForecast
import java.text.SimpleDateFormat
import java.util.Locale

class DaySelectorAdapter(
    private val days: List<DailyForecast>,
    private val onDaySelected: (DailyForecast) -> Unit
) : RecyclerView.Adapter<DaySelectorAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(val binding: ItemDaySelectorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDaySelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position]
        val context = holder.itemView.context

        // Format date: 2026-05-31T00:00:00.000000Z -> "31"
        val dayNumber = try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(day.forecastDate.substring(0, 10))
            SimpleDateFormat("dd", Locale.US).format(date!!)
        } catch (e: Exception) {
            "?"
        }

        holder.binding.tvDayName.text = day.dayName.take(3)
        holder.binding.tvDayDate.text = dayNumber

        if (selectedPosition == position) {
            holder.binding.cardDay.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary))
            holder.binding.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.binding.tvDayDate.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.binding.cardDay.strokeWidth = 0
        } else {
            holder.binding.cardDay.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            holder.binding.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
            holder.binding.tvDayDate.setTextColor(ContextCompat.getColor(context, R.color.on_surface))
            holder.binding.cardDay.strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onDaySelected(day)
        }
    }

    override fun getItemCount() = days.size
}
