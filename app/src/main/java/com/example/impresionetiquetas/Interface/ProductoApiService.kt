package com.example.impresionetiquetas.Interface

import com.example.impresionetiquetas.model.ProductoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductoApiService {
    @GET("productos/{codigo}")
    fun buscarProducto(
        @Path("codigo") codigo: String
    ): Call<ProductoResponse>
}