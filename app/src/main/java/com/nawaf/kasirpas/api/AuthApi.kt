package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.request.AuthRequest
import com.nawaf.kasirpas.request.RegisterRequest
import com.nawaf.kasirpas.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/session")
    suspend fun getSession(@Header("Authorization") token: String): Response<AuthResponse>
}