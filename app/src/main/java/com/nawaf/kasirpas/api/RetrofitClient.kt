package com.nawaf.kasirpas.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.196.30:8000/api/"//wifi hp nawaf

    // Satu mesin Retrofit untuk semua
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val productApi: ProductApi by lazy { retrofit.create(ProductApi::class.java) }
}