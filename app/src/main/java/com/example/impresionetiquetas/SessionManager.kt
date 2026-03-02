package com.example.impresionetiquetas

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("etiquetas_session", Context.MODE_PRIVATE)

    fun saveUser(usuario: String) {
        prefs.edit().putString("usuario", usuario).apply()
    }

    fun getUser(): String? {
        return prefs.getString("usuario", null)
    }

    fun isLogged(): Boolean {
        return getUser() != null
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}