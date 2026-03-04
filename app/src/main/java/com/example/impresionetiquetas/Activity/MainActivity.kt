package com.example.impresionetiquetas.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.impresionetiquetas.Objects.RetrofitClient
import com.example.impresionetiquetas.Request.PrintRequest
import com.example.impresionetiquetas.R
import com.example.impresionetiquetas.SessionManager
import com.example.impresionetiquetas.model.ProductoResponse
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    private lateinit var etCodigo: EditText
    private lateinit var tvDescripcion: TextView
    private lateinit var etCantidad: EditText
    private lateinit var tvResultado: TextView
    private lateinit var btnLogout: Button
    private lateinit var tvUsuario: TextView
    private lateinit var session: SessionManager
    private lateinit var usuario: String
    private lateinit var layoutZebra: LinearLayout
    private lateinit var layoutDatamax: LinearLayout
    private lateinit var layoutSewoo: LinearLayout
    private lateinit var tilCodigo: TextInputLayout

    private var productoActual: ProductoResponse? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etCodigo = findViewById(R.id.etCodigo)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        etCantidad = findViewById(R.id.etCantidad)
        tvResultado = findViewById(R.id.tvResultado)
        btnLogout = findViewById(R.id.btnLogout)
        tvUsuario = findViewById(R.id.tvUsuario)
        layoutZebra = findViewById(R.id.layoutZebra)
        layoutDatamax = findViewById(R.id.layoutDatamax)
        layoutSewoo = findViewById(R.id.layoutSewoo)
        tilCodigo = findViewById(R.id.tilCodigo)
        session = SessionManager(this)
        usuario = session.getUser().toString()

        tilCodigo.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT

        tilCodigo.setEndIconOnClickListener {
            limpiarPantalla()
        }

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

        etCodigo.setOnEditorActionListener { _, actionId, event ->

            if (
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.action == KeyEvent.ACTION_DOWN)
            ) {

                procesarCodigoEscaneado()
                true
            } else {
                false
            }
        }

        actualizarEstadoImpresoras(false)

    }

    private fun actualizarEstadoImpresoras(activo: Boolean) {

        val alpha = if (activo) 1f else 0.4f

        layoutZebra.alpha = alpha
        layoutDatamax.alpha = alpha
        layoutSewoo.alpha = alpha
    }

    private fun mostrarMensaje(texto: String, esError: Boolean = false) {

        tvResultado.text = texto

        if (esError) {
            tvResultado.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            tvResultado.setTextColor(getColor(android.R.color.holo_green_dark))
        }

        Handler(Looper.getMainLooper()).postDelayed({
            tvResultado.text = ""
        }, 2000)
    }

    private fun sonidoExito() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            .startTone(ToneGenerator.TONE_PROP_ACK, 150)
    }

    private fun sonidoError() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            .startTone(ToneGenerator.TONE_PROP_NACK, 200)
    }

    private fun vibrar(ms: Long = 80) {

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return

        if (!vibrator.hasVibrator()) return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(ms)
        }
    }

    private fun limpiarPantalla() {

        productoActual = null

        etCodigo.text.clear()
        tvDescripcion.text = ""
        etCantidad.text.clear()

        etCodigo.requestFocus()
        actualizarEstadoImpresoras(false)
    }

    private fun obtenerCodigo(): String {

        val texto = etCodigo.text.toString()

        return texto.split("-")[0].trim()
    }

    private fun mostrarProductoEscaneado(producto: ProductoResponse) {

        productoActual = producto

        etCodigo.setText(producto.item)
        tvDescripcion.text = producto.descripcionLocal

        etCantidad.setText("1")
        etCantidad.requestFocus()
        etCantidad.selectAll()

        actualizarEstadoImpresoras(true)
    }
    private fun buscarProducto(codigo: String) {

        RetrofitClient.productoApi.buscarProducto(codigo)
            .enqueue(object : Callback<ProductoResponse> {

                override fun onResponse(
                    call: Call<ProductoResponse>,
                    response: Response<ProductoResponse>
                ) {
                    if (response.isSuccessful) {

                        val producto = response.body()

                        producto?.let {
                            mostrarProductoEscaneado(it)
                        }
                    }
                }

                override fun onFailure(call: Call<ProductoResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al buscar producto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        etCodigo.requestFocus()
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

        Log.d("SCAN", "SE EJECUTO")

        val codigoLimpio = etCodigo.text.toString()
            .trim()
            .replace("\\s+".toRegex(), "")

        if (codigoLimpio.isEmpty()) return

        etCodigo.setText(codigoLimpio)

        buscarProducto(codigoLimpio)
    }

    private fun imprimirEtiqueta(endpoint: String, usuario: String) {

        val producto = productoActual

        if (producto == null) {
            tvResultado.text = "Escanee un producto"
            return
        }

        val cantidadTexto = etCantidad.text.toString().trim()

        if (cantidadTexto.isEmpty()) {
            tvResultado.text = "Ingrese cantidad"
            return
        }

        val request = PrintRequest(
            item = producto.item,
            cantidad = cantidadTexto.toInt(),
            usuario = usuario
        )

        RetrofitClient.printApi.imprimir(endpoint, request)
            .enqueue(object : Callback<Void> {

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {

                    runOnUiThread {

                        if (response.isSuccessful) {

                            vibrar()
                            sonidoExito()
                            mostrarMensaje("Impresión enviada ✔")

                            etCantidad.requestFocus()
                            etCantidad.selectAll()
                        } else {
                            sonidoError()
                            mostrarMensaje("Error servidor: ${response.code()}", true)
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {

                    runOnUiThread {
                        vibrar(200)
                        sonidoError()
                        mostrarMensaje("Error conexión", true)
                    }
                }
            })
    }
}