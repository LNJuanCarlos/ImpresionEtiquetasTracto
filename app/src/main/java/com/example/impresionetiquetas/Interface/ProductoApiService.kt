package com.example.impresionetiquetas.Interface

import com.example.impresionetiquetas.model.ProductoResponse
import com.example.impresionetiquetas.model.ProductoStockResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductoApiService {
    @GET("productos/{codigo}")
    fun buscarProducto(
        @Path("codigo") codigo: String
    ): Call<ProductoResponse>

    @GET("productos/stock/{item}")
    fun obtenerStock(
        @Path("item") item: String
    ): Call<List<ProductoStockResponse>>
}