package com.nawaf.kasirpas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.model.HourlyPrediction
import java.util.Locale

class HourlyForecastAdapter(private val items: List<HourlyPrediction>) :
    RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    // IP Server Laravel (Tanpa /api/)
    private val BASE_URL = "http://192.168.0.2:8000/"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHour: TextView = view.findViewById(R.id.tvHour)
        val ivStatusIcon: ImageView = view.findViewById(R.id.ivStatusIcon)
        val tvBusyLevel: TextView = view.findViewById(R.id.tvBusyLevel)
        val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
        val progressIntensity: com.google.android.material.progressindicator.LinearProgressIndicator = view.findViewById(R.id.progressIntensity)
        val layoutProductPredictions: LinearLayout = view.findViewById(R.id.layoutProductPredictions)
        val containerProducts: LinearLayout = view.findViewById(R.id.containerProducts)
        val cardHourly: com.google.android.material.card.MaterialCardView = view.findViewById(R.id.cardHourly)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvHour.text = item.hour
        holder.tvBusyLevel.text = item.busyLevel
        holder.tvEmoji.text = item.emoji

        setupBusyStyles(holder, item)

        if (item.productPredictions.isNotEmpty()) {
            holder.layoutProductPredictions.visibility = View.VISIBLE
            holder.containerProducts.removeAllViews()
            
            item.productPredictions.take(3).forEach { prediction ->
                val productView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.item_ai_product_prediction, holder.containerProducts, false)
                
                productView.findViewById<TextView>(R.id.tvProductName).text = prediction.productName
                productView.findViewById<TextView>(R.id.tvProductDemand).text = "Demand: ${if (prediction.probability.toDouble() > 0.8) "High" else "Stable"}"
                productView.findViewById<TextView>(R.id.tvEstimatedQty).text = "~${prediction.estimatedQty}"
                
                val ivProduct = productView.findViewById<ImageView>(R.id.ivProduct)
                
                // PENTING: Ambil image_url dari objek product yang ada di response API
                val rawImageUrl = prediction.product?.imageUrl
                val finalImageUrl = when {
                    rawImageUrl.isNullOrEmpty() -> null
                    rawImageUrl.startsWith("http") -> rawImageUrl // Jika URL lengkap (kayak Unsplash)
                    rawImageUrl.startsWith("storage/") -> BASE_URL + rawImageUrl // Jika sudah ada prefix storage
                    else -> BASE_URL + "storage/" + rawImageUrl.removePrefix("/") // Jika path relatif Laravel
                }

                // LOAD GAMBAR ASLI DARI RESPONSE
                ivProduct.load(finalImageUrl) {
                    crossfade(true)
                    crossfade(500)
                    // Hapus placeholder ikon box agar bener-bener kelihatan gamar produknya
                    transformations(RoundedCornersTransformation(12f))
                }
                
                holder.containerProducts.addView(productView)
            }
        } else {
            holder.layoutProductPredictions.visibility = View.GONE
        }
    }

    private fun setupBusyStyles(holder: ViewHolder, item: HourlyPrediction) {
        val context = holder.itemView.context
        holder.cardHourly.strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        holder.cardHourly.strokeColor = ContextCompat.getColor(context, R.color.outline_variant)

        when (item.busyLevel) {
            "CLOSED" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_lock)
                holder.ivStatusIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.outline)
                holder.tvBusyLevel.backgroundTintList = ContextCompat.getColorStateList(context, R.color.surface_variant)
                holder.tvBusyLevel.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))
                holder.progressIntensity.visibility = View.INVISIBLE
                holder.itemView.alpha = 0.5f
            }
            "LOW" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_person)
                holder.ivStatusIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.accent_green)
                holder.tvBusyLevel.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bg_card_light)
                holder.tvBusyLevel.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                holder.progressIntensity.visibility = View.VISIBLE
                holder.progressIntensity.progress = 15
                holder.progressIntensity.setIndicatorColor(ContextCompat.getColor(context, R.color.accent_green))
                holder.itemView.alpha = 1.0f
            }
            "MODERATE" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_person)
                holder.ivStatusIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.primary)
                holder.tvBusyLevel.backgroundTintList = ContextCompat.getColorStateList(context, R.color.primary_transparent)
                holder.tvBusyLevel.setTextColor(ContextCompat.getColor(context, R.color.primary))
                holder.progressIntensity.visibility = View.VISIBLE
                holder.progressIntensity.progress = 45
                holder.progressIntensity.setIndicatorColor(ContextCompat.getColor(context, R.color.primary))
                holder.itemView.alpha = 1.0f
            }
            "BUSY", "PEAK" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_trending_up)
                holder.ivStatusIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.primary)
                holder.tvBusyLevel.text = if (item.busyLevel == "PEAK") "BUSY PEAK" else "BUSY"
                holder.tvBusyLevel.backgroundTintList = ContextCompat.getColorStateList(context, R.color.primary)
                holder.tvBusyLevel.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.progressIntensity.visibility = View.VISIBLE
                holder.progressIntensity.progress = 90
                holder.progressIntensity.setIndicatorColor(ContextCompat.getColor(context, R.color.primary))
                holder.cardHourly.strokeWidth = (2 * context.resources.displayMetrics.density).toInt()
                holder.cardHourly.strokeColor = ContextCompat.getColor(context, R.color.primary)
                holder.itemView.alpha = 1.0f
            }
        }
    }

    override fun getItemCount() = items.size
}
