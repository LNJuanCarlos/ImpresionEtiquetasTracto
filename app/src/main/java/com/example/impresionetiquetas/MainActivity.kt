package com.example.impresionetiquetas

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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
    private lateinit var tvResultado: TextView
    private lateinit var btnLogout: Button
    private lateinit var tvUsuario: TextView
    private lateinit var session: SessionManager
    private lateinit var layoutZebra: LinearLayout
    private lateinit var layoutDatamax: LinearLayout
    private lateinit var layoutSewoo: LinearLayout

        private val baseUrl = "http://172.16.4.202:8080/api/print"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCodigo = findViewById(R.id.etCodigo)
        etCantidad = findViewById(R.id.etCantidad)

        tvResultado = findViewById(R.id.tvResultado)

        btnLogout = findViewById(R.id.btnLogout)
        tvUsuario = findViewById(R.id.tvUsuario)
        layoutZebra = findViewById(R.id.layoutZebra)
        layoutDatamax = findViewById(R.id.layoutDatamax)
        layoutSewoo = findViewById(R.id.layoutSewoo)
        session = SessionManager(this)

        val usuario = session.getUser()

        tvUsuario.text = "Usuario: $usuario"

        if (!session.isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        efectoPresion(layoutZebra) {
            imprimirEtiqueta("zebra")
        }

        efectoPresion(layoutDatamax) {
            imprimirEtiqueta("datamax")
        }

        efectoPresion(layoutSewoo) {
            imprimirEtiqueta("sewoo")
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

    private fun efectoPresion(view: View, accion: () -> Unit) {

        view.setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    v.scaleX = 0.93f
                    v.scaleY = 0.93f
                }

                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1f
                    v.scaleY = 1f
                    accion()
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }
            true
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

    private fun imprimirEtiqueta(endpoint: String) {

        val codigo = etCodigo.text.toString().trim()
        val cantidad = etCantidad.text.toString().trim()

        if (codigo.isEmpty() || cantidad.isEmpty()) {
            tvResultado.text = "Ingrese código y cantidad"
            return
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
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.doOutput = true

                conn.outputStream.use {
                    it.write(json.toByteArray())
                    it.flush()
                }

                //  IMPORTANTE
                val responseCode = conn.responseCode

                val response = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    conn.errorStream?.bufferedReader()?.readText()
                }

                runOnUiThread {
                    tvResultado.text = "Impresión enviada ✔"
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