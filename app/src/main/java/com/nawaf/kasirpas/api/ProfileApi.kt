package com.nawaf.kasirpas.api

import com.nawaf.kasirpas.response.ProfileDeleteResponse
import com.nawaf.kasirpas.response.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @Multipart
    @POST("profile")
    suspend fun createProfile(
        @Header("Authorization") token: String,
        @Part("bio") bio: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<ProfileResponse>

    @Multipart
    @POST("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("_method") method: RequestBody,
        @Part("bio") bio: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<ProfileResponse>

    @DELETE("profile")
    suspend fun deleteProfile(
        @Header("Authorization") token: String
    ): Response<ProfileDeleteResponse>
}
