package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.request.AuthRequest
import com.nawaf.kasirpas.request.RegisterRequest
import com.nawaf.kasirpas.response.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}