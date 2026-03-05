package com.example.impresionetiquetas.model

data class PrintRequest(
    val item: String,
    val cantidad: Int,
    val usuario: String
)