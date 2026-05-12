package com.nawaf.kasirpas.activity

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.adapter.HistoryAdapter
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.databinding.ActivityHistoryBinding
import com.nawaf.kasirpas.utils.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private lateinit var prefManager: PreferenceManager
    
    private var currentPage = 1
    private var lastPage = 1
    private var isLoading = false
    private var searchJob: Job? = null
    private var currentFilter = "ALL" // ALL, TODAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)
        setupRecyclerView()
        setupBottomNav()
        setupListeners()
        setupSearch()
        
        fetchHistory(1, true)
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        binding.rvHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && currentPage < lastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        fetchHistory(currentPage + 1)
                    }
                }
            }
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500)
                    fetchHistory(1, true)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_laporan
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_kasir -> { finish(); true }
                R.id.nav_checkout -> true
                R.id.nav_laporan -> true
                R.id.nav_pengaturan -> true
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.chipAll.setOnClickListener {
            if (currentFilter != "ALL") {
                currentFilter = "ALL"
                updateFilterUI()
                fetchHistory(1, true)
            }
        }

        binding.chipToday.setOnClickListener {
            if (currentFilter != "TODAY") {
                currentFilter = "TODAY"
                updateFilterUI()
                fetchHistory(1, true)
            }
        }
    }

    private fun updateFilterUI() {
        if (currentFilter == "ALL") {
            binding.chipAll.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_container))
            binding.tvChipAll.setTextColor(ContextCompat.getColor(this, R.color.on_primary_container))
            
            binding.chipToday.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_container_high))
            binding.tvChipToday.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))
        } else {
            binding.chipToday.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_container))
            binding.tvChipToday.setTextColor(ContextCompat.getColor(this, R.color.on_primary_container))
            
            binding.chipAll.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_container_high))
            binding.tvChipAll.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant))
        }
    }

    private fun fetchHistory(page: Int = 1, isRefresh: Boolean = false) {
        val token = prefManager.getToken() ?: return
        val searchQuery = binding.etSearch.text.toString()
        
        isLoading = true
        if (isRefresh) {
            currentPage = 1
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.reportApi.getSalesHistory(
                    token = "Bearer $token",
                    page = page,
                    search = if (searchQuery.isNotEmpty()) searchQuery else null,
                    filter = if (currentFilter != "ALL") currentFilter.lowercase() else null
                )
                
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    data?.let {
                        if (isRefresh) {
                            adapter.setItems(it.transactions)
                        } else {
                            adapter.addItems(it.transactions)
                        }
                        
                        currentPage = it.currentPage
                        lastPage = it.lastPage
                        binding.tvPaginationInfo.text = "Halaman $currentPage dari $lastPage"
                    }
                } else {
                    Toast.makeText(this@HistoryActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
