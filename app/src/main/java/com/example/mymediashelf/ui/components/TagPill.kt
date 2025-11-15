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
import androidx.compose.ui.unit.dp
import com.mymediashelf.app.ui.theme.BorderDark
import com.mymediashelf.app.ui.theme.SurfaceDark

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
            .background(SurfaceDark)
            .border(1.dp, BorderDark, RoundedCornerShape(999.dp))
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
            color = MaterialTheme.colorScheme.onSurface
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tags.isEmpty()) {
            Text(
                text = "No tags",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            tags.forEach { tag ->
                TagPill(
                    text = tag,
                    onRemove = onTagRemove?.let { { it(tag) } }
                )
            }
        }
    }
}