package com.example.impresionetiquetas

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
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

    private val urlApi = "http://172.16.4.202:8080/api/print/zebra/item"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCodigo = findViewById(R.id.etCodigo)
        etCantidad = findViewById(R.id.etCantidad)
        btnImprimir = findViewById(R.id.btnImprimir)
        tvResultado = findViewById(R.id.tvResultado)

        btnImprimir.setOnClickListener {
            imprimirEtiqueta()
        }

        //  Foco inicial en código
        etCodigo.requestFocus()

        //  Cuando el scanner presiona ENTER
        etCodigo.setOnEditorActionListener { _, _, event ->

            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {

                procesarCodigoEscaneado()
                true
            } else {
                false
            }
        }
    }

    //  Procesa el código escaneado
    private fun procesarCodigoEscaneado() {

        val codigoLimpio = etCodigo.text.toString()
            .trim()
            .replace("\\s+".toRegex(), "") // elimina espacios, saltos, tabs

        if (codigoLimpio.isEmpty()) return

        // Mostrar código limpio
        etCodigo.setText(codigoLimpio)

        //  Cantidad por defecto = 1
        etCantidad.setText("1")

        //  Mover foco a cantidad
        etCantidad.requestFocus()

        // Opcional: seleccionar el 1 para reemplazar rápido
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
                    tvResultado.text = "Impresión enviada ✔"

                    //  Limpiar campos para siguiente escaneo
                    etCodigo.text.clear()
                    etCantidad.text.clear()

                    //  Volver foco a código
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