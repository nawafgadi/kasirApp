package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.request.BillingRequest
import com.nawaf.kasirpas.response.ActiveSubscriptionResponse
import com.nawaf.kasirpas.response.SubscribeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BillingApi {
    @POST("billing/subscribe")
    suspend fun subscribe(
        @Header("Authorization") token: String,
        @Body request: BillingRequest
    ): Response<SubscribeResponse>

    @GET("billing/active")
    suspend fun getActiveSubscription(
        @Header("Authorization") token: String
    ): Response<ActiveSubscriptionResponse>
}
