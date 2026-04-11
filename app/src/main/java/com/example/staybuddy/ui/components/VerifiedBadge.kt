package com.example.staybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val VerifiedBlue = Color(0xFF1DA1F2)

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    showLabel: Boolean = false
) {
    if (showLabel) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            color = VerifiedBlue.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = VerifiedBlue,
                    modifier = Modifier.size(size)
                )
                Text(
                    text = "Verified",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VerifiedBlue
                )
            }
        }
    } else {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Verified",
            tint = VerifiedBlue,
            modifier = modifier.size(size)
        )
    }
}
