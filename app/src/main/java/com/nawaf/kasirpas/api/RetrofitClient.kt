package com.nawaf.kasirpas.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.2:8000/api/"

    // Satu mesin Retrofit untuk semua
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
}