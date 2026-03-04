package com.example.impresionetiquetas.Objects

import com.example.impresionetiquetas.Interface.PrintApiService
import com.example.impresionetiquetas.Interface.ProductoApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://172.16.4.202:8080/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val printApi: PrintApiService by lazy {
        retrofit.create(PrintApiService::class.java)
    }

    val productoApi: ProductoApiService by lazy {
        retrofit.create(ProductoApiService::class.java)
    }
}