package com.example.impresionetiquetas.model

import android.content.Context

class PrinterManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SEWOO = "sewoo_selected"
    }

    fun guardarSewoo(numero: Int) {
        prefs.edit().putInt(KEY_SEWOO, numero).apply()
    }

    fun obtenerSewoo(): Int? {
        val value = prefs.getInt(KEY_SEWOO, -1)
        return if (value == -1) null else value
    }

    fun limpiar() {
        prefs.edit().remove(KEY_SEWOO).apply()
    }
}