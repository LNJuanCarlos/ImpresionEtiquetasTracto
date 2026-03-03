package com.example.impresionetiquetas.Interface

import com.example.impresionetiquetas.Request.PrintRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PrintApi {

    @POST("{printer}/item")
    fun imprimir(
        @Path("printer") printer: String,
        @Body request: PrintRequest
    ): Call<Void>
}