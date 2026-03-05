package com.example.impresionetiquetas.model

data class ProductoStockResponse(

    val almacen: String,
    val item: String,
    val descripcion: String,
    val unidad: String,
    val ubicacion: String,
    val stockActual: Double
)
