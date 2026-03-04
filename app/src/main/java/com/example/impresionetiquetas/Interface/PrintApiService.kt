package com.example.impresionetiquetas.Interface

import com.example.impresionetiquetas.Request.PrintRequest
import com.example.impresionetiquetas.model.ProductoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PrintApiService {

    @POST("print/{printer}/item")
    fun imprimir(
        @Path("printer") printer: String,
        @Body request: PrintRequest
    ): Call<Void>

}