package com.example.staybuddy.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppUtils {
    /**
     * Opens WhatsApp with a specific phone number and message.
     * Automatically handles 10-digit Indian numbers by prepending "91".
     */
    fun openWhatsApp(context: Context, phoneNumber: String, message: String = "") {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "Phone number is missing", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Remove any non-digit characters
            val digitsOnly = phoneNumber.replace(Regex("\\D"), "")
            
            // For Indian users: if it's 10 digits, prepend 91 (WhatsApp requires country code)
            val finalNumber = if (digitsOnly.length == 10) "91$digitsOnly" else digitsOnly
            
            val encodedMsg = java.net.URLEncoder.encode(message, "UTF-8")
            
            // Try app-specific URI first (whatsapp://)
            val whatsappUri = Uri.parse("whatsapp://send?phone=$finalNumber&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web URL (wa.me) if app scheme fails (e.g., app not installed)
            try {
                val digitsOnly = phoneNumber.replace(Regex("\\D"), "")
                val finalNumber = if (digitsOnly.length == 10) "91$digitsOnly" else digitsOnly
                val webUrl = "https://wa.me/$finalNumber?text=${java.net.URLEncoder.encode(message, "UTF-8")}"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Could not open WhatsApp. Make sure it's installed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens the default phone dialer.
     */
    fun makePhoneCall(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "Phone number is missing", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer.", Toast.LENGTH_SHORT).show()
        }
    }
}
