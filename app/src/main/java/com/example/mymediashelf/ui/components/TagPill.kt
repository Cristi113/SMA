package com.mymediashelf.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.mymediashelf.app.ui.theme.BorderDark
import com.mymediashelf.app.ui.theme.PrimaryLight
import com.mymediashelf.app.ui.theme.TextPrimary

@Composable
fun TagPill(
    text: String,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PrimaryLight.copy(alpha = 0.2f))
            .border(1.dp, PrimaryLight.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
        if (onRemove != null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove tag",
                modifier = Modifier
                    .size(12.dp)
                    .clickable(onClick = onRemove)
                    .padding(2.dp),
                tint = Color(0xFFFECACA)
            )
        }
    }
}

@Composable
fun TagPillsRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
    onTagRemove: ((String) -> Unit)? = null
) {
    if (tags.isEmpty()) {
        Text(
            text = "No tags",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = modifier
        )
    } else {
        Layout(
            modifier = modifier,
            content = {
                tags.forEach { tag ->
                    TagPill(
                        text = tag,
                        onRemove = onTagRemove?.let { { it(tag) } }
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
            var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
            var currentRowWidth = 0
            val horizontalSpacing = 8.dp.roundToPx()
            
            placeables.forEach { placeable ->
                val placeableWidth = placeable.width + horizontalSpacing
                if (currentRowWidth + placeableWidth > constraints.maxWidth && currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf(placeable)
                    currentRowWidth = placeable.width
                } else {
                    currentRow.add(placeable)
                    currentRowWidth += placeableWidth
                }
            }
            if (currentRow.isNotEmpty()) {
                rows.add(currentRow)
            }
            
            val rowHeight = placeables.maxOfOrNull { it.height } ?: 0
            val verticalSpacing = 8.dp.roundToPx()
            val totalHeight = if (rows.isEmpty()) 0 else rows.size * rowHeight + (rows.size - 1) * verticalSpacing
            
            layout(constraints.maxWidth, totalHeight.coerceAtLeast(constraints.minHeight)) {
                var y = 0
                rows.forEach { row ->
                    var x = 0
                    row.forEach { placeable ->
                        placeable.placeRelative(x, y)
                        x += placeable.width + horizontalSpacing
                    }
                    y += rowHeight + verticalSpacing
                }
            }
        }
    }
}