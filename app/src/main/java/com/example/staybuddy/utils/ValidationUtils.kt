package com.example.staybuddy.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6 && password.any { it.isDigit() }
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(" ", "").replace("-", "")
        return cleaned.length == 10 && cleaned.all { it.isDigit() }
    }

    fun isValidPrice(price: String): Boolean {
        return price.toIntOrNull()?.let { it > 0 } ?: false
    }
}
