package com.example.facefit.data.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("FaceFitPrefs", Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        println("DEBUG: Saving token: ${token.take(10)}...")
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString("auth_token", null)
        if (token.isNullOrBlank()) {
            println("DEBUG: Token is null or blank")
            return null
        }

        // Basic validation (adjust based on your token format)
        if (token.split('.').size != 3) {
            println("DEBUG: Token appears malformed (invalid JWT structure)")
            clearToken()
            return null
        }

        return token
    }

    fun clearToken() {
        println("DEBUG: Clearing token")
        sharedPreferences.edit().remove("auth_token").apply()
    }
}