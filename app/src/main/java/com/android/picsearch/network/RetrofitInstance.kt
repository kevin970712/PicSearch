package com.android.picsearch.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitInstance {
    val api: TmpFilesApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://tmpfiles.org/")
            .addConverterFactory(Json {
                ignoreUnknownKeys = true
            }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmpFilesApiService::class.java)
    }
}