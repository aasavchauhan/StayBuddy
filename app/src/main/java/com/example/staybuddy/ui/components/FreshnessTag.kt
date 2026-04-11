package com.example.staybuddy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

private val FreshGreen = Color(0xFF2E7D32)
private val StaleYellow = Color(0xFFF9A825)
private val OldRed = Color(0xFFC62828)

@Composable
fun FreshnessTag(
    createdAt: Long,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val diff = now - createdAt
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    val (label, color) = when {
        days < 1 -> "Today" to FreshGreen
        days == 1L -> "Yesterday" to FreshGreen
        days < 7 -> "$days days ago" to FreshGreen
        days < 14 -> "1 week ago" to StaleYellow
        days < 30 -> "${days / 7} weeks ago" to StaleYellow
        days < 60 -> "1 month ago" to OldRed
        else -> "${days / 30} months ago" to OldRed
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
