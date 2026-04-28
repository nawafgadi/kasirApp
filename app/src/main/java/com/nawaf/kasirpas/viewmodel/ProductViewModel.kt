package com.nawaf.kasirpas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawaf.kasirpas.api.RetrofitClient
import com.nawaf.kasirpas.model.Product
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    var quantity: Int
)

class ProductViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Cart State
    private val _cartItems = MutableLiveData<List<CartItem>>(listOf())
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> get() = _toastMessage

    private var allProductsList: List<Product> = listOf()

    fun loadProducts(token: String, forceRefresh: Boolean = false) {
        if (allProductsList.isNotEmpty() && !forceRefresh) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.productApi.getProducts("Bearer $token")
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: listOf()
                    allProductsList = data
                    _products.value = data
                    _error.value = null
                } else {
                    _error.value = "Gagal memuat produk"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCart.find { it.product.id == product.id }
        
        val availableStock = product.stocks.sumOf { it.stock_on_hand }
        val currentQuantity = existingItem?.quantity ?: 0

        if (currentQuantity < availableStock) {
            if (existingItem != null) {
                existingItem.quantity += 1
            } else {
                currentCart.add(CartItem(product, 1))
            }
            _cartItems.value = currentCart
        } else {
            _toastMessage.value = "Stok tidak mencukupi! Maksimal: $availableStock"
        }
    }

    fun removeFromCart(product: Product) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity -= 1
            } else {
                currentCart.remove(existingItem)
            }
        }
        _cartItems.value = currentCart
    }

    fun clearCart() {
        _cartItems.value = listOf()
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun getCartTotal(): Double {
        return _cartItems.value?.sumOf { 
            (it.product.price.toDoubleOrNull() ?: 0.0) * it.quantity 
        } ?: 0.0
    }

    fun getCartItemCount(): Int {
        return _cartItems.value?.sumOf { it.quantity } ?: 0
    }

    fun getFilteredProducts(query: String, categoryId: Int?): List<Product> {
        var filtered = allProductsList
        if (categoryId != null) {
            filtered = filtered.filter { it.category_id == categoryId }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter { it.name.contains(query, ignoreCase = true) }
        }
        return filtered
    }
}
