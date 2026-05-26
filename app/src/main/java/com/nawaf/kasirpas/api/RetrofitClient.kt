package com.nawaf.kasirpas.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
 //   private const val BASE_URL = "http://192.168.0.2:8000/api/" // wifi rumah siam
//    private const val BASE_URL = "http://192.168.0.2:8000/api/"
//    private const val BASE_URL = "http://192.168.0.2:8000/api/"
//private const val BASE_URL = "http://192.168.1.7:8000/api/"// rona
 //private const val BASE_URL = "http://10.212.77.134:8000/api/"
    private const val BASE_URL = "https://kitty-uncompromised-audiometrically.ngrok-free.dev/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val productApi: ProductApi by lazy { retrofit.create(ProductApi::class.java) }
    val billingApi: BillingApi by lazy { retrofit.create(BillingApi::class.java) }
    val transactionApi: TransactionApi by lazy { retrofit.create(TransactionApi::class.java) }
    val categoryApi: CategoryApi by lazy { retrofit.create(CategoryApi::class.java) }
    val aiApi: AiApi by lazy { retrofit.create(AiApi::class.java) }
    val reportApiService: ReportApiService by lazy { retrofit.create(ReportApiService::class.java) }
    val profileApi: ProfileApi by lazy { retrofit.create(ProfileApi::class.java) }
}
