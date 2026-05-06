package com.nawaf.kasirpas.response

import com.nawaf.kasirpas.model.Product

data class ProductResponse(
    val message: String,
    val data: List<Product>
)

data class SingleProductResponse(
    val message: String,
    val data: Product
)
