package com.mymediashelf.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.mymediashelf.app.R
import com.mymediashelf.app.ui.components.*
import com.mymediashelf.app.ui.theme.*
import com.mymediashelf.app.ui.viewmodel.HomeViewModel
import com.mymediashelf.app.ui.viewmodel.viewModelWithFactory

@Composable
fun HomeScreen(
    onNavigateToItems: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToLists: () -> Unit,
    viewModel: HomeViewModel = viewModelWithFactory()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientBackgroundStart,
                        GradientBackgroundMiddle,
                        GradientBackgroundSecond,
                        GradientBackgroundEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeroSection(
                onNavigateToItems = onNavigateToItems,
                onNavigateToLists = onNavigateToLists
            )

            QuickAccessGrid(
                onItemsClick = onNavigateToItems,
                onTagsClick = onNavigateToTags,
                onListsClick = onNavigateToLists
            )

            LibrarySnapshot(
                itemCount = uiState.itemCount,
                tagCount = uiState.tagCount,
                listCount = uiState.listCount,
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
fun HeroSection(
    onNavigateToItems: () -> Unit,
    onNavigateToLists: () -> Unit
) {
    GradientCard(
        gradientColors = listOf(
            GradientStartIndigo.copy(alpha = 0.25f),
            BackgroundDark.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            Text(
                text = context.getString(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            )
            Text(
                text = context.getString(R.string.home_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val context = LocalContext.current
                Button(
                    onClick = onNavigateToItems,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text(
                        context.getString(R.string.home_open_items),
                        color = Color(0xFFFFFFFF)
                    )
                }
                OutlinedButton(
                    onClick = onNavigateToLists,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1E3A8A)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E3A8A))
                ) {
                    Text(
                        context.getString(R.string.home_view_lists),
                        color = Color(0xFF1E3A8A)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessGrid(
    onItemsClick: () -> Unit,
    onTagsClick: () -> Unit,
    onListsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val context = LocalContext.current
            QuickAccessCard(
                title = context.getString(R.string.nav_items),
                subtitle = context.getString(R.string.home_items_desc),
                icon = Icons.Default.Movie,
                onClick = onItemsClick,
                modifier = Modifier.weight(1f)
            )
            QuickAccessCard(
                title = context.getString(R.string.nav_tags),
                subtitle = context.getString(R.string.home_tags_desc),
                icon = Icons.Default.Label,
                onClick = onTagsClick,
                modifier = Modifier.weight(1f)
            )
        }
        val context = LocalContext.current
        QuickAccessCard(
            title = context.getString(R.string.nav_lists),
            subtitle = context.getString(R.string.home_lists_desc),
            icon = Icons.Default.List,
            onClick = onListsClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColors = listOf(
        com.mymediashelf.app.ui.theme.CardBackgroundPrimary,
        com.mymediashelf.app.ui.theme.CardBackgroundSecondary,
        com.mymediashelf.app.ui.theme.CardBackgroundPrimary
    )
    val borderColors = listOf(
        com.mymediashelf.app.ui.theme.CardBorderPrimary,
        com.mymediashelf.app.ui.theme.CardBorderSecondary,
        com.mymediashelf.app.ui.theme.CardBorderPrimary
    )

    val index = when (title) {
        "Items" -> 0
        "Tags" -> 1
        "Lists" -> 2
        else -> 0
    }

    MediaCard(
        onClick = onClick,
        modifier = modifier,
        cardColor = cardColors[index],
        borderColor = borderColors[index]
    ){
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            val context = LocalContext.current
            Text(
                text = context.getString(R.string.home_section_items),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Center
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF78350F)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LibrarySnapshot(
    itemCount: Int,
    tagCount: Int,
    listCount: Int,
    isLoading: Boolean
){
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val context = LocalContext.current
        Text(
            text = context.getString(R.string.home_library_snapshot),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF000000),
            textAlign = TextAlign.Center
        )
        MediaCard(
            cardColor = com.mymediashelf.app.ui.theme.CardBackgroundPrimary,
            borderColor = com.mymediashelf.app.ui.theme.CardBorderPrimary
        ){
            if (isLoading){
                LoadingIndicator()
            }else{
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    val context = LocalContext.current
                    Text(
                        text = context.getString(R.string.home_stat_items, itemCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF000000),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = context.getString(R.string.home_stat_tags, tagCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF000000),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = context.getString(R.string.home_stat_lists, listCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF000000),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}