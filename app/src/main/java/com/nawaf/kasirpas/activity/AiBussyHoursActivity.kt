package com.nawaf.kasirpas.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.adapter.HourlyForecastAdapter
import com.nawaf.kasirpas.adapter.DaySelectorAdapter
import com.nawaf.kasirpas.databinding.ActivityAiBussyHoursBinding
import com.nawaf.kasirpas.model.DailyForecast
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiBussyHoursActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiBussyHoursBinding
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        binding = ActivityAiBussyHoursBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PreferenceManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        fetchData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }

        binding.rvHourlyForecast.layoutManager = LinearLayoutManager(this)
        binding.rvDays.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun fetchData() {
        val token = prefManager.getToken() ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.aiApi.getLatestBusyHours("Bearer $token")
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val aiRun = response.body()?.data
                    if (aiRun != null && aiRun.dailyForecasts.isNotEmpty()) {
                        val allDays = aiRun.dailyForecasts
                        
                        // Menentukan hari "Sekarang" untuk dijadikan default.
                        // Jika dalam mode testing/mock data 2026, kita cari tanggal yang paling mendekati 'hari ini'
                        // atau langsung ambil index 0 jika data pertama adalah target hari ini.
                        
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val todayStr = sdf.format(Date())
                        
                        // Logika pencarian index hari ini
                        var startIndex = allDays.indexOfFirst { it.forecastDate.startsWith(todayStr) }
                        
                        // Jika hari ini tidak ada di data (mungkin karena timezone atau data mock), 
                        // kita default ke index 0 (karena biasanya data pertama adalah yang terbaru/hari ini)
                        if (startIndex == -1) startIndex = 0
                        
                        // Ambil 3 hari kedepan saja sesuai request
                        val limitedDays = allDays.drop(startIndex).take(3)
                        
                        if (limitedDays.isNotEmpty()) {
                            setupDaySelector(limitedDays)
                            displayData(limitedDays[0])
                        }
                    } else {
                        Toast.makeText(this@AiBussyHoursActivity, "Data AI belum tersedia", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AiBussyHoursActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                e.printStackTrace()
                Toast.makeText(this@AiBussyHoursActivity, "Terjadi kesalahan koneksi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDaySelector(days: List<DailyForecast>) {
        val adapter = DaySelectorAdapter(days) { selectedDay ->
            displayData(selectedDay)
        }
        binding.rvDays.adapter = adapter
    }

    private fun displayData(forecast: DailyForecast) {
        binding.layoutContent.visibility = View.VISIBLE
        
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dateStr = try {
            val date = inputFormat.parse(forecast.forecastDate.substring(0, 10))
            outputFormat.format(date!!)
        } catch (e: Exception) {
            forecast.forecastDate
        }
        
        binding.tvDate.text = dateStr
        binding.tvPeakHour.text = forecast.peakHour
        binding.tvPredictedTrx.text = forecast.totalPredictedTrx
        
        val revenue = forecast.totalPredictedRevenue.toDoubleOrNull() ?: 0.0
        val formattedRevenue = if (revenue >= 1000) {
            String.format(Locale.US, "Rp %.2fk", revenue / 1000)
        } else {
            String.format(Locale.US, "Rp %.0f", revenue)
        }
        binding.tvPredictedRevenue.text = formattedRevenue

        binding.tvInsightsTitle.text = "${forecast.dayName}'s Insights"

        val adapter = HourlyForecastAdapter(forecast.hourlyPredictions)
        binding.rvHourlyForecast.adapter = adapter
    }
}
