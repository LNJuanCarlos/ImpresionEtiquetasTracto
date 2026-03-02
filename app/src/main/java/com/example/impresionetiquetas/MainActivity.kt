package com.example.impresionetiquetas

import android.content.Intent
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
import com.example.impresionetiquetas.Activity.LoginActivity
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var etCantidad: EditText
    private lateinit var btnImprimir: Button
    private lateinit var tvResultado: TextView
    private lateinit var spImpresora: Spinner

    private lateinit var btnLogout: Button

    private lateinit var tvUsuario: TextView

    private lateinit var session: SessionManager

        private val baseUrl = "http://172.16.4.202:8080/api/print"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCodigo = findViewById(R.id.etCodigo)
        etCantidad = findViewById(R.id.etCantidad)
        btnImprimir = findViewById(R.id.btnImprimir)
        tvResultado = findViewById(R.id.tvResultado)
        spImpresora = findViewById(R.id.spImpresora)
        btnLogout = findViewById(R.id.btnLogout)
        tvUsuario = findViewById(R.id.tvUsuario)
        session = SessionManager(this)

        val usuario = session.getUser()

        tvUsuario.text = "Usuario: $usuario"

        if (!session.isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

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

        btnLogout.setOnClickListener {
            cerrarSesion()
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

    private fun cerrarSesion() {

        // borrar datos de sesión
        session.logout()

        // volver al login limpiando historial
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
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

                val usuario = session.getUser()

                val json = """
                {
                "item":"$codigo",
                "cantidad":$cantidad,
                "usuario":"$usuario"
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

                val responseCode = conn.responseCode

                val response = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    conn.errorStream.bufferedReader().readText()
                }

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