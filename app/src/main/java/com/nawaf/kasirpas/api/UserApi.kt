package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.model.User
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): User
}