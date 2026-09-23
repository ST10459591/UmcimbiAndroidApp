package com.example.umcimbisa.api

import com.example.umcimbisa.model.ApiUser
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("users")
    suspend fun getUsers(
        @Query("name") name: String? = null
    ): List<ApiUser>
}