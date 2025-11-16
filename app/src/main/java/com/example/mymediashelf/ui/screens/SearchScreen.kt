package com.mymediashelf.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.mymediashelf.app.ui.theme.GradientItemsEnd
import com.mymediashelf.app.ui.theme.GradientItemsMiddle
import com.mymediashelf.app.ui.theme.GradientItemsStart
import com.mymediashelf.app.R
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.ItemStatus
import com.mymediashelf.app.domain.model.ItemType
import com.mymediashelf.app.ui.components.*
import com.mymediashelf.app.ui.viewmodel.ItemsViewModel
import com.mymediashelf.app.ui.viewmodel.viewModelWithFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: ItemsViewModel = viewModelWithFactory()
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<ItemType?>(null) }
    var selectedStatus by remember { mutableStateOf<ItemStatus?>(null) }
    var selectedYear by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf<Long?>(null) }
    var selectedListId by remember { mutableStateOf<Long?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }

    var expandedType by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedTag by remember { mutableStateOf(false) }
    var expandedList by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            val context = LocalContext.current
            TopAppBar(
                title = { Text(context.getString(R.string.search_title)) }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientItemsStart,
                            GradientItemsMiddle,
                            GradientItemsEnd
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    MediaCard(
                        cardColor = com.mymediashelf.app.ui.theme.CardBackgroundPrimary,
                        borderColor = com.mymediashelf.app.ui.theme.CardBorderPrimary
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val context = LocalContext.current
                            Text(
                                text = context.getString(R.string.search_filters),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color(0xFF000000),
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(context.getString(R.string.items_search_hint), color = Color(0xFF1F2937)) },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF000000),
                                    unfocusedTextColor = Color(0xFF1F2937),
                                    focusedLabelColor = Color(0xFF1F2937),
                                    unfocusedLabelColor = Color(0xFF6B7280)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedType,
                                    onExpandedChange = { expandedType = !expandedType },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = selectedType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: context.getString(R.string.items_filter_all_types),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(context.getString(R.string.items_type_label), color = Color(0xFF1F2937)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF000000),
                                            unfocusedTextColor = Color(0xFF1F2937),
                                            focusedLabelColor = Color(0xFF1F2937),
                                            unfocusedLabelColor = Color(0xFF6B7280)
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedType,
                                        onDismissRequest = { expandedType = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(context.getString(R.string.items_filter_all_types)) },
                                            onClick = {
                                                selectedType = null
                                                expandedType = false
                                            }
                                        )
                                        ItemType.entries.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                                onClick = {
                                                    selectedType = type
                                                    expandedType = false
                                                }
                                            )
                                        }
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = expandedStatus,
                                    onExpandedChange = { expandedStatus = !expandedStatus },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = selectedStatus?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: context.getString(R.string.items_filter_all_status),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(context.getString(R.string.items_status_label), color = Color(0xFF1F2937)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF000000),
                                            unfocusedTextColor = Color(0xFF1F2937),
                                            focusedLabelColor = Color(0xFF1F2937),
                                            unfocusedLabelColor = Color(0xFF6B7280)
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedStatus,
                                        onDismissRequest = { expandedStatus = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(context.getString(R.string.items_filter_all_status)) },
                                            onClick = {
                                                selectedStatus = null
                                                expandedStatus = false
                                            }
                                        )
                                        ItemStatus.entries.forEach { status ->
                                            DropdownMenuItem(
                                                text = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                                onClick = {
                                                    selectedStatus = status
                                                    expandedStatus = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = selectedYear,
                                onValueChange = { selectedYear = it },
                                label = { Text(context.getString(R.string.search_year), color = Color(0xFF1F2937)) },
                                placeholder = { Text(context.getString(R.string.items_year_hint), color = Color(0xFF6B7280)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF000000),
                                    unfocusedTextColor = Color(0xFF1F2937),
                                    focusedLabelColor = Color(0xFF1F2937),
                                    unfocusedLabelColor = Color(0xFF6B7280)
                                )
                            )

                            if (uiState.allTags.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedTag,
                                    onExpandedChange = { expandedTag = !expandedTag },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = uiState.allTags.find { it.id == selectedTagId }?.name ?: context.getString(R.string.search_all_tags),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(context.getString(R.string.items_tags), color = Color(0xFF1F2937)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTag) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF000000),
                                            unfocusedTextColor = Color(0xFF1F2937),
                                            focusedLabelColor = Color(0xFF1F2937),
                                            unfocusedLabelColor = Color(0xFF6B7280)
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedTag,
                                        onDismissRequest = { expandedTag = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(context.getString(R.string.search_all_tags)) },
                                            onClick = {
                                                selectedTagId = null
                                                expandedTag = false
                                            }
                                        )
                                        uiState.allTags.forEach { tag ->
                                            DropdownMenuItem(
                                                text = { Text(tag.name) },
                                                onClick = {
                                                    selectedTagId = tag.id
                                                    expandedTag = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = favoritesOnly,
                                    onCheckedChange = { favoritesOnly = it }
                                )
                                Text(context.getString(R.string.items_filter_favorites_only), color = Color(0xFF1F2937))
                            }

                            if (uiState.allLists.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedList,
                                    onExpandedChange = { expandedList = !expandedList },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = uiState.allLists.find { it.id == selectedListId }?.name ?: "All lists",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("List", color = Color(0xFF1F2937)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedList) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF000000),
                                            unfocusedTextColor = Color(0xFF1F2937),
                                            focusedLabelColor = Color(0xFF1F2937),
                                            unfocusedLabelColor = Color(0xFF6B7280)
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedList,
                                        onDismissRequest = { expandedList = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All lists") },
                                            onClick = {
                                                selectedListId = null
                                                expandedList = false
                                            }
                                        )
                                        uiState.allLists.forEach { list ->
                                            DropdownMenuItem(
                                                text = { Text(list.name) },
                                                onClick = {
                                                    selectedListId = list.id
                                                    expandedList = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.updateSearchQuery(searchQuery)
                                    viewModel.updateTypeFilter(selectedType)
                                    viewModel.updateStatusFilter(selectedStatus)
                                    viewModel.updateFavoritesOnly(favoritesOnly)
                                    viewModel.updateYearFilter(selectedYear.toIntOrNull())
                                    viewModel.updateTagFilter(selectedTagId)
                                    viewModel.updateListFilter(selectedListId)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E3A8A),
                                    contentColor = Color(0xFFFFFFFF)
                                )
                            ) {
                                Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFFFFF))
                                Spacer(Modifier.width(8.dp))
                                Text(context.getString(R.string.search_button), color = Color(0xFFFFFFFF))
                            }
                        }
                    }
                }

                if (uiState.isLoading && uiState.items.isEmpty()) {
                    item {
                        LoadingIndicator()
                    }
                } else if (uiState.items.isEmpty()) {
                    item {
                        val context = LocalContext.current
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = context.getString(R.string.search_no_results)
                        )
                    }
                } else {
                    items(uiState.items) { item ->
                        ItemCard(
                            item = item,
                            allTags = uiState.allTags,
                            onUpdate = viewModel::updateItem,
                            onDelete = viewModel::deleteItem,
                            onAddTag = { itemId, tagId ->
                                viewModel.addTagToItem(itemId, tagId)
                            },
                            onRemoveTag = { itemId, tagId ->
                                viewModel.removeTagFromItem(itemId, tagId)
                            }
                        )
                    }
                }
            }
        }
    }
}