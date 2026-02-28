package com.example.impresionetiquetas

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var etCantidad: EditText
    private lateinit var btnImprimir: Button
    private lateinit var tvResultado: TextView
    private lateinit var spImpresora: Spinner

    private val baseUrl = "http://172.16.4.202:8080/api/print"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCodigo = findViewById(R.id.etCodigo)
        etCantidad = findViewById(R.id.etCantidad)
        btnImprimir = findViewById(R.id.btnImprimir)
        tvResultado = findViewById(R.id.tvResultado)
        spImpresora = findViewById(R.id.spImpresora)

        // Opciones de impresora
        val impresoras = arrayOf("Zebra", "Datamax", "Sewoo")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, impresoras
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spImpresora.adapter = adapter

        btnImprimir.setOnClickListener {
            imprimirEtiqueta()
        }

        etCodigo.requestFocus()

        etCodigo.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                procesarCodigoEscaneado()
                true
            } else {
                false
            }
        }
    }

    private fun procesarCodigoEscaneado() {

        val codigoLimpio = etCodigo.text.toString()
            .trim()
            .replace("\\s+".toRegex(), "")

        if (codigoLimpio.isEmpty()) return

        etCodigo.setText(codigoLimpio)
        etCantidad.setText("1")
        etCantidad.requestFocus()
        etCantidad.selectAll()
    }

    private fun imprimirEtiqueta() {

        val codigo = etCodigo.text.toString()
            .trim()
            .replace("\\s+".toRegex(), "")

        val cantidad = etCantidad.text.toString().trim()

        if (codigo.isEmpty() || cantidad.isEmpty()) {
            tvResultado.text = "Ingrese código y cantidad"
            return
        }

        val impresoraSeleccionada = spImpresora.selectedItem.toString()

        val endpoint = when (impresoraSeleccionada) {
            "Zebra" -> "zebra"
            "Datamax" -> "datamax"
            "Sewoo" -> "sewoo"
            else -> "zebra"
        }

        val urlApi = "$baseUrl/$endpoint/item"


        Thread {
            try {

                val json = """
                {
                    "item":"$codigo",
                    "cantidad":$cantidad
                }
                """.trimIndent()

                val url = URL(urlApi)
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use {
                    it.write(json.toByteArray())
                }

                conn.inputStream.bufferedReader().readText()

                runOnUiThread {
                    tvResultado.text = "Impresión enviada a $impresoraSeleccionada ✔"

                    etCodigo.text.clear()
                    etCantidad.text.clear()
                    etCodigo.requestFocus()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    tvResultado.text = "Error: ${e.message}"
                }
            }
        }.start()
    }
}