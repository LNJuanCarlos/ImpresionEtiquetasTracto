package com.example.impresionetiquetas.Activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.impresionetiquetas.Objects.RetrofitClient
import com.example.impresionetiquetas.Request.PrintRequest
import com.example.impresionetiquetas.R
import com.example.impresionetiquetas.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var etCantidad: EditText
    private lateinit var tvResultado: TextView
    private lateinit var btnLogout: Button
    private lateinit var tvUsuario: TextView
    private lateinit var session: SessionManager
    private lateinit var usuario: String
    private lateinit var layoutZebra: LinearLayout
    private lateinit var layoutDatamax: LinearLayout
    private lateinit var layoutSewoo: LinearLayout


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
        usuario = session.getUser().toString()



        tvUsuario.text = "Usuario: $usuario"

        if (!session.isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        efectoPresion(layoutZebra) {
            imprimirEtiqueta("zebra", usuario)
        }

        efectoPresion(layoutDatamax) {
            imprimirEtiqueta("datamax", usuario)
        }

        efectoPresion(layoutSewoo) {
            imprimirEtiqueta("sewoo", usuario)
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

    private fun imprimirEtiqueta(endpoint: String, usuario: String) {

        val codigo = etCodigo.text.toString().trim()
        val cantidad = etCantidad.text.toString().trim()

        if (codigo.isEmpty() || cantidad.isEmpty()) {
            tvResultado.text = "Ingrese código y cantidad"
            return
        }

        val request = PrintRequest(
            item = codigo,
            cantidad = cantidad.toInt(),
            usuario = usuario
        )

        RetrofitClient.api.imprimir(endpoint, request)
            .enqueue(object : Callback<Void> {

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {

                    runOnUiThread {

                        if (response.isSuccessful) {
                            tvResultado.text = "Impresión enviada ✔"

                            etCodigo.text.clear()
                            etCantidad.text.clear()
                            etCodigo.requestFocus()

                        } else {
                            tvResultado.text =
                                "Error servidor: ${response.code()}"
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {

                    runOnUiThread {
                        tvResultado.text = "Error conexión: ${t.message}"
                    }
                }
            })
    }
}