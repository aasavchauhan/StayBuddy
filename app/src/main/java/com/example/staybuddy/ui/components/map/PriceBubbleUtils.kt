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

        val priceText = formatCompactPrice(price)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (if (isSelected) 13f else 12f) * scale
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            color = if (isSelected) Color.WHITE else Color.parseColor("#1B1B1F")
        }

        val textWidth = textPaint.measureText(priceText)
        val textHeight = textPaint.descent() - textPaint.ascent()

        val paddingX = (if (isSelected) 14f else 10f) * scale
        val paddingY = (if (isSelected) 8f else 6f) * scale

        val bgWidth = textWidth + paddingX * 2
        val bgHeight = textHeight + paddingY * 2

        // Pointer triangle
        val pointerHeight = 6f * scale
        val pointerWidth = 10f * scale

        val shadowRadius = if (isSelected) 6f * scale else 3f * scale
        val dy = 2f * scale

        val totalWidth = (bgWidth + shadowRadius * 2).toInt()
        val totalHeight = (bgHeight + shadowRadius * 2 + dy + pointerHeight).toInt()

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background paint with shadow
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) Color.parseColor("#0D47A1") else Color.WHITE
            setShadowLayer(shadowRadius, 0f, dy, Color.parseColor(if (isSelected) "#60000000" else "#30000000"))
        }

        val drawRect = RectF(shadowRadius, shadowRadius, shadowRadius + bgWidth, shadowRadius + bgHeight)
        val cornerRadius = bgHeight / 2f

        canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, bgPaint)

        // Pointer triangle at bottom center
        val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) Color.parseColor("#0D47A1") else Color.WHITE
            style = Paint.Style.FILL
        }
        val pointerPath = Path().apply {
            val cx = totalWidth / 2f
            val top = shadowRadius + bgHeight
            moveTo(cx - pointerWidth / 2, top)
            lineTo(cx + pointerWidth / 2, top)
            lineTo(cx, top + pointerHeight)
            close()
        }
        canvas.drawPath(pointerPath, pointerPaint)

        // Border for non-selected
        if (!isSelected) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D0D0D0")
                style = Paint.Style.STROKE
                strokeWidth = 1f * scale
            }
            canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, strokePaint)
        }

        // Draw selected indicator dot
        if (isSelected) {
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64B5F6")
            }
            canvas.drawCircle(
                shadowRadius + 6f * scale,
                shadowRadius + bgHeight / 2f,
                2.5f * scale,
                dotPaint
            )
        }

        val textX = shadowRadius + paddingX + (if (isSelected) 3f * scale else 0f)
        val textY = shadowRadius + paddingY - textPaint.ascent()

        canvas.drawText(priceText, textX, textY, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    fun createClusterDrawable(context: Context, count: Int): Drawable {
        val scale = context.resources.displayMetrics.density
        val size = (48f * scale).toInt()

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Outer ring
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A0D47A1")
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, outerPaint)

        // Inner circle
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0D47A1")
            setShadowLayer(4f * scale, 0f, 2f * scale, Color.parseColor("#40000000"))
        }
        val innerRadius = size / 2f - 4f * scale
        canvas.drawCircle(size / 2f, size / 2f, innerRadius, innerPaint)

        // Count text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = (if (count > 99) 11f else 14f) * scale
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val countText = if (count > 99) "99+" else count.toString()
        val textY = size / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText(countText, size / 2f, textY, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun formatCompactPrice(price: Int): String {
        return when {
            price >= 100000 -> "₹${price / 100000}L"
            price >= 1000 -> "₹${String.format("%.1f", price / 1000.0).removeSuffix(".0")}k"
            else -> "₹$price"
        }
    }
}
