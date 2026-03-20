package com.example.impresionetiquetas.Interface

import com.example.impresionetiquetas.model.PrintRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PrintApiService {

    @POST("print/{printer}/item")
    fun imprimir(
        @Path("printer") printer: String,
        @Body request: PrintRequest
    ): Call<Void>


    @POST("print/sewoo/item")
    fun imprimirSewoo(
        @Query("impresora") impresora: Int,
        @Body request: PrintRequest
    ): Call<Void>

}