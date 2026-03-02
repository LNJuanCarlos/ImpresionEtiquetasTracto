package com.example.impresionetiquetas.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.impresionetiquetas.MainActivity
import com.example.impresionetiquetas.R
import com.example.impresionetiquetas.SessionManager
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvEstado: TextView

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
                conn.doOutput = true

                conn.outputStream.use {
                    it.write(json.toByteArray())
                }

                val response = conn.inputStream.bufferedReader().readText()

                //  guardar sesión
                SessionManager(this).saveUser(usuario)

                runOnUiThread {
                    abrirMain()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    tvEstado.text = "Login incorrecto"
                }
            }

        }.start()
    }

    private fun abrirMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}