package com.mymediashelf.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.mymediashelf.app.ui.theme.GradientItemsEnd
import com.mymediashelf.app.ui.theme.GradientItemsMiddle
import com.mymediashelf.app.ui.theme.GradientItemsStart
import com.mymediashelf.app.R
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.ItemStatus
import com.mymediashelf.app.domain.model.ItemType
import com.mymediashelf.app.ui.components.*
import com.mymediashelf.app.ui.utils.rememberShakeDetector
import com.mymediashelf.app.ui.viewmodel.ItemsViewModel
import com.mymediashelf.app.ui.viewmodel.viewModelWithFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    onNavigateToSearch: () -> Unit = {},
    viewModel: ItemsViewModel = viewModelWithFactory()
) {
    val uiState by viewModel.uiState.collectAsState()

    rememberShakeDetector(
        onShake = {
            if (uiState.items.isNotEmpty()) {
                viewModel.getRandomItem()
            }
        }
    )

    Scaffold(
        topBar = {
            val context = LocalContext.current
            TopAppBar(
                title = { Text(context.getString(R.string.items_screen_title)) },
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch
                    ) {
                        Icon(Icons.Default.Search, contentDescription = context.getString(R.string.search_screen_title))
                    }
                }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AddItemSection(
                    allTags = uiState.allTags,
                    onCreateTag = { tagName ->
                        viewModel.createTag(tagName)
                    },
                    onAddItem = { item ->
                        viewModel.addItem(item)
                    }
                )

                FiltersSection(
                    searchQuery = uiState.searchQuery,
                    selectedType = uiState.selectedType,
                    selectedStatus = uiState.selectedStatus,
                    favoritesOnly = uiState.favoritesOnly,
                    allLists = uiState.allLists,
                    selectedListId = uiState.selectedListId,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onTypeChange = viewModel::updateTypeFilter,
                    onStatusChange = viewModel::updateStatusFilter,
                    onFavoritesOnlyChange = viewModel::updateFavoritesOnly,
                    onListChange = viewModel::updateListFilter
                )

                if (uiState.isLoading && uiState.items.isEmpty()) {
                    LoadingIndicator()
                } else if (uiState.items.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Movie,
                        title = if (uiState.searchQuery.isNotBlank() ||
                            uiState.selectedType != null ||
                            uiState.selectedStatus != null ||
                            uiState.favoritesOnly) {
                            "No items match your filters"
                        } else {
                            "No items yet"
                        }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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

    if (uiState.showRandomDialog) {
        RandomItemDialog(
            item = uiState.randomItem,
            onDismiss = { viewModel.dismissRandomDialog() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSection(
    allTags: List<com.mymediashelf.app.domain.model.Tag>,
    onCreateTag: (String) -> Unit,
    onAddItem: (Item) -> Unit
) {
    var itemTitle by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf(ItemType.MOVIE) }
    var itemYear by remember { mutableStateOf("") }
    var itemStatus by remember { mutableStateOf(ItemStatus.PLANNED) }
    var itemFavorite by remember { mutableStateOf(false) }
    var itemRating by remember { mutableStateOf("") }
    var itemComment by remember { mutableStateOf("") }
    var tagInputMode by remember { mutableStateOf(false) }
    var selectedTagId by remember { mutableStateOf<Long?>(null) }
    var newTagName by remember { mutableStateOf("") }

    var expandedType by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedTag by remember { mutableStateOf(false) }

    MediaCard(
        cardColor = com.mymediashelf.app.ui.theme.CardBackgroundPrimary,
        borderColor = com.mymediashelf.app.ui.theme.CardBorderPrimary
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            Text(
                text = context.getString(R.string.items_add_new),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            OutlinedTextField(
                value = itemTitle,
                onValueChange = { itemTitle = it },
                label = { Text(context.getString(R.string.items_title_label), color = Color(0xFF1F2937)) },
                placeholder = { Text(context.getString(R.string.items_title_hint), color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Movie, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF1F2937),
                    focusedLabelColor = Color(0xFF1F2937),
                    unfocusedLabelColor = Color(0xFF6B7280)
                )
            )

            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = itemType.name.lowercase().replaceFirstChar { it.uppercase() },
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
                    ItemType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                itemType = type
                                expandedType = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = itemYear,
                onValueChange = { itemYear = it },
                label = { Text(context.getString(R.string.items_year_label), color = Color(0xFF1F2937)) },
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

            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = itemStatus.name.lowercase().replaceFirstChar { it.uppercase() },
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
                    ItemStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                itemStatus = status
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = itemFavorite,
                    onCheckedChange = { itemFavorite = it }
                )
                Text(context.getString(R.string.items_favorite_label), color = Color(0xFF1F2937))
            }

            OutlinedTextField(
                value = itemRating,
                onValueChange = { itemRating = it },
                label = { Text(context.getString(R.string.items_rating_label), color = Color(0xFF1F2937)) },
                placeholder = { Text(context.getString(R.string.items_rating_hint), color = Color(0xFF6B7280)) },
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

            OutlinedTextField(
                value = itemComment,
                onValueChange = { itemComment = it },
                label = { Text(context.getString(R.string.items_comment_label), color = Color(0xFF1F2937)) },
                placeholder = { Text(context.getString(R.string.items_comment_hint), color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF1F2937),
                    focusedLabelColor = Color(0xFF1F2937),
                    unfocusedLabelColor = Color(0xFF6B7280)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!tagInputMode) {
                    if (allTags.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedTag,
                            onExpandedChange = { expandedTag = !expandedTag },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = allTags.find { it.id == selectedTagId }?.name ?: "Select tag",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tag", color = Color(0xFF1F2937)) },
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
                                    text = { Text("No tag") },
                                    onClick = {
                                        selectedTagId = null
                                        expandedTag = false
                                    }
                                )
                                allTags.forEach { tag ->
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
                    IconButton(
                        onClick = { tagInputMode = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create new tag", tint = Color(0xFF1E3A8A))
                    }
                } else {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("New tag name", color = Color(0xFF1F2937)) },
                        placeholder = { Text("Enter tag name", color = Color(0xFF6B7280)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF000000),
                            unfocusedTextColor = Color(0xFF1F2937),
                            focusedLabelColor = Color(0xFF1F2937),
                            unfocusedLabelColor = Color(0xFF6B7280)
                        )
                    )
                    IconButton(
                        onClick = {
                            tagInputMode = false
                            newTagName = ""
                            selectedTagId = null
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFF1F2937))
                    }
                }
            }

            Button(
                onClick = {
                    if (itemTitle.isNotBlank()) {
                        var finalTag: com.mymediashelf.app.domain.model.Tag? = null

                        if (tagInputMode && newTagName.isNotBlank()) {
                            val newTag = com.mymediashelf.app.domain.model.Tag(name = newTagName.trim())
                            onCreateTag(newTagName.trim())
                            finalTag = allTags.find { it.name.equals(newTagName.trim(), ignoreCase = true) }
                            if (finalTag == null) {
                                finalTag = newTag
                            }
                        } else if (selectedTagId != null) {
                            finalTag = allTags.find { it.id == selectedTagId }
                        }

                        onAddItem(
                            Item(
                                title = itemTitle.trim(),
                                type = itemType,
                                year = itemYear.toIntOrNull(),
                                status = itemStatus,
                                favorite = itemFavorite,
                                rating = itemRating.toIntOrNull()?.takeIf { it in 1..10 },
                                comment = itemComment.trim().takeIf { it.isNotBlank() },
                                tags = finalTag?.let { listOf(it) } ?: emptyList()
                            )
                        )
                        itemTitle = ""
                        itemType = ItemType.MOVIE
                        itemYear = ""
                        itemStatus = ItemStatus.PLANNED
                        itemFavorite = false
                        itemRating = ""
                        itemComment = ""
                        selectedTagId = null
                        newTagName = ""
                        tagInputMode = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500),
                    contentColor = Color(0xFF5D4037)
                )
            ) {
                val context = LocalContext.current
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color(0xFF5D4037))
                Spacer(Modifier.width(8.dp))
                Text(context.getString(R.string.items_add_button), color = Color(0xFF5D4037))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSection(
    searchQuery: String,
    selectedType: ItemType?,
    selectedStatus: ItemStatus?,
    favoritesOnly: Boolean,
    allLists: List<com.mymediashelf.app.domain.model.MediaList>,
    selectedListId: Long?,
    onSearchQueryChange: (String) -> Unit,
    onTypeChange: (ItemType?) -> Unit,
    onStatusChange: (ItemStatus?) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onListChange: (Long?) -> Unit
) {
    MediaCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search title", color = Color(0xFF1F2937)) },
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
                var expandedType by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedType?.name?.lowercase() ?: "All types",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type", color = Color(0xFF1F2937)) },
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
                            text = { Text("All types") },
                            onClick = { onTypeChange(null); expandedType = false }
                        )
                        ItemType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase()) },
                                onClick = { onTypeChange(type); expandedType = false }
                            )
                        }
                    }
                }

                var expandedStatus by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedStatus?.name?.lowercase() ?: "All status",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status", color = Color(0xFF1F2937)) },
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
                            text = { Text("All status") },
                            onClick = { onStatusChange(null); expandedStatus = false }
                        )
                        ItemStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name.lowercase()) },
                                onClick = { onStatusChange(status); expandedStatus = false }
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = favoritesOnly,
                    onCheckedChange = onFavoritesOnlyChange
                )
                Text("Favorites only", color = Color(0xFF1F2937))
            }

            if (allLists.isNotEmpty()) {
                var expandedList by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedList,
                    onExpandedChange = { expandedList = !expandedList },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = allLists.find { it.id == selectedListId }?.name ?: "All lists",
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
                                onListChange(null)
                                expandedList = false
                            }
                        )
                        allLists.forEach { list ->
                            DropdownMenuItem(
                                text = { Text(list.name) },
                                onClick = {
                                    onListChange(list.id)
                                    expandedList = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(
    item: Item,
    allTags: List<com.mymediashelf.app.domain.model.Tag>,
    onUpdate: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onAddTag: (Long, Long) -> Unit,
    onRemoveTag: (Long, Long) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(item.title) }
    var editedType by remember { mutableStateOf(item.type) }
    var editedYear by remember { mutableStateOf(item.year?.toString() ?: "") }
    var editedStatus by remember { mutableStateOf(item.status) }
    var editedFavorite by remember { mutableStateOf(item.favorite) }
    var editedRating by remember { mutableStateOf(item.rating?.toString() ?: "") }
    var editedComment by remember { mutableStateOf(item.comment ?: "") }

    MediaCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text("Title", color = Color(0xFF1F2937)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF000000),
                        unfocusedTextColor = Color(0xFF1F2937),
                        focusedLabelColor = Color(0xFF1F2937),
                        unfocusedLabelColor = Color(0xFF6B7280)
                    )
                )

                var expandedType by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editedType.name.lowercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type", color = Color(0xFF1F2937)) },
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
                        ItemType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase()) },
                                onClick = { editedType = type; expandedType = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = editedYear,
                    onValueChange = { editedYear = it },
                    label = { Text("Year", color = Color(0xFF1F2937)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF000000),
                        unfocusedTextColor = Color(0xFF1F2937),
                        focusedLabelColor = Color(0xFF1F2937),
                        unfocusedLabelColor = Color(0xFF6B7280)
                    )
                )

                var expandedStatus by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editedStatus.name.lowercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status", color = Color(0xFF1F2937)) },
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
                        ItemStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name.lowercase()) },
                                onClick = { editedStatus = status; expandedStatus = false }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = editedFavorite,
                        onCheckedChange = { editedFavorite = it }
                    )
                    Text("Favorite", color = Color(0xFF1F2937))
                }

                OutlinedTextField(
                    value = editedRating,
                    onValueChange = { editedRating = it },
                    label = { Text("Rating (1-10)", color = Color(0xFF1F2937)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF000000),
                        unfocusedTextColor = Color(0xFF1F2937),
                        focusedLabelColor = Color(0xFF1F2937),
                        unfocusedLabelColor = Color(0xFF6B7280)
                    )
                )

                OutlinedTextField(
                    value = editedComment,
                    onValueChange = { editedComment = it },
                    label = { Text("Comment", color = Color(0xFF1F2937)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
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
                    Button(
                        onClick = {
                            val updatedItem = item.copy(
                                title = editedTitle,
                                type = editedType,
                                year = editedYear.toIntOrNull(),
                                status = editedStatus,
                                favorite = editedFavorite,
                                rating = editedRating.toIntOrNull()?.takeIf { it in 1..10 },
                                comment = editedComment.takeIf { it.isNotBlank() }
                            )
                            onUpdate(updatedItem)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        val context = LocalContext.current
                        Text(context.getString(R.string.items_save), color = Color(0xFFFFFFFF))
                    }
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1E3A8A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E3A8A))
                    ) {
                        val context = LocalContext.current
                        Text(context.getString(R.string.cancel), color = Color(0xFF1E3A8A))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = "${item.type.name.lowercase()} • ${item.year ?: "N/A"} • ${item.status.name.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1F2937)
                        )
                        if (item.rating != null) {
                            Text(
                                text = "⭐ ${item.rating}/10",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1F2937)
                            )
                        }
                        if (item.comment != null) {
                            Text(
                                text = item.comment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF000000)
                            )
                        }
                        if (item.favorite) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Favorite",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                TagPillsRow(
                    tags = item.tags.map { it.name },
                    onTagRemove = { tagName ->
                        val tag = item.tags.find { it.name == tagName }
                        tag?.let { onRemoveTag(item.id, it.id) }
                    }
                )

                var expandedTagSelect by remember { mutableStateOf(false) }
                val availableTags = allTags.filter { tag ->
                    !item.tags.any { it.id == tag.id }
                }

                if (availableTags.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedTagSelect,
                        onExpandedChange = { expandedTagSelect = !expandedTagSelect }
                    ) {
                        OutlinedTextField(
                            value = "+ Add tag",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTagSelect) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTagSelect,
                            onDismissRequest = { expandedTagSelect = false }
                        ) {
                            availableTags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag.name) },
                                    onClick = {
                                        onAddTag(item.id, tag.id)
                                        expandedTagSelect = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFFFFF))
                        Spacer(Modifier.width(4.dp))
                        val context = LocalContext.current
                        Text(context.getString(R.string.edit), color = Color(0xFFFFFFFF))
                    }
                    OutlinedButton(
                        onClick = { onDelete(item) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        val context = LocalContext.current
                        Text(context.getString(R.string.items_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}