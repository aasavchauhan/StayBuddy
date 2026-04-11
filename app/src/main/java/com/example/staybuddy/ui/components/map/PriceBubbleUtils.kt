package com.example.staybuddy.ui.components.map

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.text.NumberFormat
import java.util.Locale

object PriceBubbleUtils {
    fun createPriceBubbleDrawable(context: Context, price: Int, isSelected: Boolean): Drawable {
        val scale = context.resources.displayMetrics.density
        
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        // e.g. "₹ 15,000" or similar
        val priceText = format.format(price)
        
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (if (isSelected) 16f else 14f) * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = if (isSelected) Color.WHITE else Color.BLACK
        }
        
        val textWidth = textPaint.measureText(priceText)
        val textHeight = textPaint.descent() - textPaint.ascent()
        
        val paddingX = (if (isSelected) 16f else 12f) * scale
        val paddingY = (if (isSelected) 10f else 6f) * scale
        
        val bgWidth = textWidth + paddingX * 2
        val bgHeight = textHeight + paddingY * 2
        
        val shadowRadius = 4f * scale
        val dy = 2f * scale
        
        val totalWidth = (bgWidth + shadowRadius * 2).toInt()
        val totalHeight = (bgHeight + shadowRadius * 2 + dy).toInt()
        
        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw shadow layer
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) Color.parseColor("#000000") else Color.WHITE
            setShadowLayer(shadowRadius, 0f, dy, Color.parseColor("#40000000"))
        }
        
        val drawRect = RectF(shadowRadius, shadowRadius, shadowRadius + bgWidth, shadowRadius + bgHeight)
        val cornerRadius = bgHeight / 2 // Pill shape
        
        // Shadow and background
        canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, shadowPaint)
        
        if (!isSelected) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0E0E0")
                style = Paint.Style.STROKE
                strokeWidth = 1f * scale
            }
            canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, strokePaint)
        }
        
        val textX = shadowRadius + paddingX
        val textY = shadowRadius + paddingY - textPaint.ascent()
        
        canvas.drawText(priceText, textX, textY, textPaint)
        
        return BitmapDrawable(context.resources, bitmap)
    }
}
