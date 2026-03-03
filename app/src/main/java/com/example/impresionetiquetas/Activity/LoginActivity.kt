package com.example.impresionetiquetas.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.impresionetiquetas.Activity.MainActivity
import com.example.impresionetiquetas.R
import com.example.impresionetiquetas.SessionManager
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    private val urlLogin =
        "http://172.16.4.202:8080/api/authentication/etiquetas/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        //  Si ya está logueado → entra directo
        if (SessionManager(this).isLogged()) {
            abrirMain()
        }

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {

        val usuario = etUsuario.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false

        Thread {

            try {

                val json = """
            {
                "usuario":"$usuario",
                "password":"$password"
            }
            """.trimIndent()

                val url = URL(urlLogin)
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.doOutput = true

                conn.outputStream.use {
                    it.write(json.toByteArray())
                }

                val responseCode = conn.responseCode

                val response = if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().readText()
                } else {
                    conn.errorStream?.bufferedReader()?.readText()
                }

                runOnUiThread {

                    btnLogin.isEnabled = true

                    if (responseCode in 200..299) {

                        //  Login correcto
                        SessionManager(this).saveUser(usuario)
                        abrirMain()

                    } else {

                        //  Login incorrecto
                        Toast.makeText(
                            this,
                            "Usuario o contraseña incorrectos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {
                    btnLogin.isEnabled = true

                    Toast.makeText(
                        this,
                        "Error de conexión con el servidor",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }.start()
    }

    private fun abrirMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}