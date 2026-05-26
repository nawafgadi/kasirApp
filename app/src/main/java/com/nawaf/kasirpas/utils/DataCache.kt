package com.nawaf.kasirpas.utils

import com.nawaf.kasirpas.model.Category
import com.nawaf.kasirpas.model.Product

object DataCache {
    var categories: List<Category>? = null
    var products: List<Product>? = null

    fun clear() {
        categories = null
        products = null
    }
}
