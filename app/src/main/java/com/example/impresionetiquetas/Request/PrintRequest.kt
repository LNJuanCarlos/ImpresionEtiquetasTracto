package com.example.impresionetiquetas.Request

data class PrintRequest(
    val item: String,
    val cantidad: Int,
    val usuario: String
)