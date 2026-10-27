package com.axenvoir.sktplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val categoryColors = listOf(
    Color(0xFF2D6CDF),
    Color(0xFF2E7D32),
    Color(0xFF8E24AA),
    Color(0xFFEF6C00),
    Color(0xFF00838F),
    Color(0xFFC62828),
)

@Composable
fun CategoryAvatar(name: String, modifier: Modifier = Modifier) {
    val color = categoryColors[(name.hashCode().let { if (it < 0) -it else it }) % categoryColors.size]
    Box(
        modifier = modifier
            .size(28.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().take(1).uppercase(),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
