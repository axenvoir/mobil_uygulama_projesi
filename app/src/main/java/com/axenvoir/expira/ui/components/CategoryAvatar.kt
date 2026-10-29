package com.axenvoir.expira.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategoryAvatar(name: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val categoryColors = listOf(
        scheme.primaryContainer to scheme.onPrimaryContainer,
        scheme.secondaryContainer to scheme.onSecondaryContainer,
        scheme.tertiaryContainer to scheme.onTertiaryContainer,
        scheme.errorContainer to scheme.onErrorContainer,
        scheme.surfaceVariant to scheme.onSurfaceVariant,
    )
    val (container, content) = categoryColors[Math.floorMod(name.hashCode(), categoryColors.size)]
    Box(
        modifier = modifier
            .size(40.dp)
            .background(container, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().take(1).uppercase().ifBlank { "•" },
            color = content,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
