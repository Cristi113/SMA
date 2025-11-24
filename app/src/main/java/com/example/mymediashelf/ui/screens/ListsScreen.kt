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
import androidx.compose.ui.text.style.TextAlign
import com.mymediashelf.app.ui.theme.GradientListsEnd
import com.mymediashelf.app.ui.theme.GradientListsMiddle
import com.mymediashelf.app.ui.theme.GradientListsStart
import com.mymediashelf.app.R
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.MediaList
import com.mymediashelf.app.ui.components.*
import com.mymediashelf.app.ui.viewmodel.ListsViewModel
import com.mymediashelf.app.ui.viewmodel.viewModelWithFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: ListsViewModel = viewModelWithFactory()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedList = uiState.selectedList
    var showListDialog by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(selectedList?.id) {
        if (selectedList != null) {
            selectedItemIds = selectedList.items.map { it.id }.toSet()
            showListDialog = true
        } else {
            showListDialog = false
        }
    }

    Scaffold(
        topBar = {
            val context = LocalContext.current
            TopAppBar(
                title = { Text(context.getString(R.string.lists_title)) }
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
                            GradientListsStart,
                            GradientListsMiddle,
                            GradientListsEnd
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
                AddListSection(
                    allItems = uiState.allItems,
                    onCreateList = { name, items ->
                        viewModel.createList(name, items)
                    }
                )

                val context = LocalContext.current
                Text(
                    text = context.getString(R.string.lists_your_lists),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                if (uiState.isLoading && uiState.lists.isEmpty()) {
                    LoadingIndicator()
                } else if (uiState.lists.isEmpty()) {
                    val context = LocalContext.current
                    EmptyState(
                        icon = Icons.Default.List,
                        title = context.getString(R.string.lists_no_lists)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.lists) { list ->
                            ListItemCard(
                                list = list,
                                isSelected = uiState.selectedList?.id == list.id,
                                onSelect = { viewModel.selectList(list) },
                                onUpdate = viewModel::updateList,
                                onDelete = viewModel::deleteList
                            )
                        }
                    }
                }

                if (uiState.lists.isNotEmpty() && selectedList == null) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    val context = LocalContext.current
                    EmptyState(
                        icon = Icons.Default.List,
                        title = context.getString(R.string.lists_no_list_selected),
                        message = context.getString(R.string.lists_select_or_create)
                    )
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

    if (showListDialog && selectedList != null) {
        AlertDialog(
            onDismissRequest = { 
                showListDialog = false
            },
            title = {
                Text(
                    "Select Items for ${selectedList.name}",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.allItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${item.type.name.lowercase()} • ${item.year ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Checkbox(
                                checked = selectedItemIds.contains(item.id),
                                onCheckedChange = { checked ->
                                    selectedItemIds = if (checked) {
                                        selectedItemIds + item.id
                                    } else {
                                        selectedItemIds - item.id
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { 
                        showListDialog = false
                    }) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.updateListItems(selectedList.id, selectedItemIds.toList())
                            showListDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA500),
                            contentColor = Color(0xFF5D4037)
                        )
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp), tint = Color(0xFF5D4037))
                        Spacer(Modifier.width(4.dp))
                        Text("Save ${selectedItemIds.size} item(s)", color = Color(0xFF5D4037))
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListSection(
    allItems: List<Item>,
    onCreateList: (String, List<Item>) -> Unit
) {
    var listName by remember { mutableStateOf("") }
    var expandedItemSelect by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

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
                text = context.getString(R.string.lists_add_new),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text(context.getString(R.string.lists_name_label), color = Color(0xFF1F2937)) },
                placeholder = { Text(context.getString(R.string.lists_name_hint), color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.List, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF1F2937),
                    focusedLabelColor = Color(0xFF1F2937),
                    unfocusedLabelColor = Color(0xFF6B7280)
                )
            )

            if (allItems.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expandedItemSelect,
                    onExpandedChange = { expandedItemSelect = !expandedItemSelect },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (selectedItemIds.isEmpty()) {
                            "Select items (optional)"
                        } else {
                            "${selectedItemIds.size} item(s) selected"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Add items", color = Color(0xFF1F2937)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedItemSelect) },
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
                        expanded = expandedItemSelect,
                        onDismissRequest = { expandedItemSelect = false }
                    ) {
                        allItems.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${item.title} (${item.type.name.lowercase()})")
                                        Checkbox(
                                            checked = selectedItemIds.contains(item.id),
                                            onCheckedChange = { checked ->
                                                selectedItemIds = if (checked) {
                                                    selectedItemIds + item.id
                                                } else {
                                                    selectedItemIds - item.id
                                                }
                                            }
                                        )
                                    }
                                },
                                onClick = {
                                    selectedItemIds = if (selectedItemIds.contains(item.id)) {
                                        selectedItemIds - item.id
                                    } else {
                                        selectedItemIds + item.id
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (selectedItemIds.isNotEmpty()) {
                Text(
                    text = "${selectedItemIds.size} item(s) selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1F2937)
                )
            }

            Button(
                onClick = {
                    if (listName.isNotBlank()) {
                        val selectedItems = allItems.filter { selectedItemIds.contains(it.id) }
                        onCreateList(listName, selectedItems)
                        listName = ""
                        selectedItemIds = emptySet()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A8A),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                val context = LocalContext.current
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFFFFF))
                Spacer(Modifier.width(8.dp))
                Text(context.getString(R.string.lists_create_button), color = Color(0xFFFFFFFF))
            }
        }
    }
}

@Composable
fun ListItemCard(
    list: MediaList,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onUpdate: (MediaList) -> Unit,
    onDelete: (MediaList) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(list.name) }

    MediaCard(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        cardColor = if (isSelected) {
            com.mymediashelf.app.ui.theme.CardBackgroundPrimary
        } else {
            com.mymediashelf.app.ui.theme.CardBackgroundSecondary
        },
        borderColor = if (isSelected) {
            com.mymediashelf.app.ui.theme.Primary
        } else {
            com.mymediashelf.app.ui.theme.Secondary
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = {
                        val context = LocalContext.current
                        Text(context.getString(R.string.lists_name_label), color = Color(0xFF1F2937))
                    },
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
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            onUpdate(list.copy(name = editedName.trim()))
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFFFFF))
                        Spacer(Modifier.width(4.dp))
                        Text(context.getString(R.string.lists_save), color = Color(0xFFFFFFFF))
                    }
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1E3A8A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E3A8A))
                    ) {
                        Text(context.getString(R.string.cancel), color = Color(0xFF1E3A8A))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF000000)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { isEditing = true }
                        ) {
                            Icon(Icons.Default.Edit, null)
                        }
                        IconButton(
                            onClick = { onDelete(list) }
                        ) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContentSection(
    list: MediaList,
    allItems: List<Item>,
    onAddItem: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onSaveItems: (List<Long>) -> Unit
){
    var expandedItemSelect by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(list.id, list.items.size) {
        selectedItemIds = list.items.map { it.id }.toSet()
    }

    val availableItems = allItems.filter { item ->
        !list.items.any { it.id == item.id }
    }

    MediaCard(
        cardColor = com.mymediashelf.app.ui.theme.CardBackgroundPrimary,
        borderColor = com.mymediashelf.app.ui.theme.CardBorderPrimary
    ){
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = context.getString(R.string.lists_list_title, list.name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { showSaveDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA500),
                        contentColor = Color(0xFF5D4037)
                    )
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp), tint = Color(0xFF5D4037))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (list.items.isEmpty()) {
                            "Save to List"
                        } else {
                            "Save to List (${list.items.size})"
                        },
                        color = Color(0xFF5D4037)
                    )
                }
            }

            if (showSaveDialog) {
                LaunchedEffect(showSaveDialog) {
                    if (showSaveDialog) {
                        selectedItemIds = list.items.map { it.id }.toSet()
                    }
                }

                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = {
                        Text(
                            "Select Items for ${list.name}",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(allItems) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${item.type.name.lowercase()} • ${item.year ?: "N/A"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                    Checkbox(
                                        checked = selectedItemIds.contains(item.id),
                                        onCheckedChange = { checked ->
                                            selectedItemIds = if (checked) {
                                                selectedItemIds + item.id
                                            } else {
                                                selectedItemIds - item.id
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ){
                            TextButton(onClick = { showSaveDialog = false }) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    onSaveItems(selectedItemIds.toList())
                                    showSaveDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFA500),
                                    contentColor = Color(0xFF5D4037)
                                )
                            ) {
                                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp), tint = Color(0xFF5D4037))
                                Spacer(Modifier.width(4.dp))
                                Text("Save ${selectedItemIds.size} item(s)", color = Color(0xFF5D4037))
                            }
                        }
                    }
                )
            }

            if (availableItems.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    ExposedDropdownMenuBox(
                        expanded = expandedItemSelect,
                        onExpandedChange = { expandedItemSelect = !expandedItemSelect },
                        modifier = Modifier.fillMaxWidth()
                    ){
                        val context = LocalContext.current
                        OutlinedTextField(
                            value = if (selectedItemIds.isEmpty()) {
                                context.getString(R.string.lists_select_item)
                            } else {
                                "${selectedItemIds.size} item(s) selected"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select items to add", color = Color(0xFF1F2937)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedItemSelect) },
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
                            expanded = expandedItemSelect,
                            onDismissRequest = { expandedItemSelect = false }
                        ) {
                            availableItems.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${item.title} (${item.type.name.lowercase()})")
                                            Checkbox(
                                                checked = selectedItemIds.contains(item.id),
                                                onCheckedChange = { checked ->
                                                    selectedItemIds = if (checked) {
                                                        selectedItemIds + item.id
                                                    } else {
                                                        selectedItemIds - item.id
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedItemIds = if (selectedItemIds.contains(item.id)) {
                                            selectedItemIds - item.id
                                        } else {
                                            selectedItemIds + item.id
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedItemIds.isNotEmpty()) {
                            Button(
                                onClick = {
                                    selectedItemIds.forEach { itemId ->
                                        onAddItem(itemId)
                                    }
                                    selectedItemIds = emptySet()
                                    expandedItemSelect = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color(0xFFFFFFFF)
                                )
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFFFFF))
                                Spacer(Modifier.width(4.dp))
                                Text("Add ${selectedItemIds.size}", color = Color(0xFFFFFFFF))
                            }
                        } else {
                            Button(
                                onClick = { expandedItemSelect = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E3A8A),
                                    contentColor = Color(0xFFFFFFFF)
                                )
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFFFFF))
                                Spacer(Modifier.width(8.dp))
                                val context = LocalContext.current
                                Text(context.getString(R.string.lists_add_item), color = Color(0xFFFFFFFF))
                            }
                        }
                    }
                }
            } else if (allItems.isEmpty()) {
                val context = LocalContext.current
                Text(
                    text = "No items available to add",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }

            if (list.items.isEmpty()) {
                val context = LocalContext.current
                EmptyState(
                    icon = Icons.Default.Movie,
                    title = context.getString(R.string.lists_no_items)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list.items) { item ->
                        ItemInListCard(
                            item = item,
                            onRemove = { onRemoveItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemInListCard(
    item: Item,
    onRemove: () -> Unit
){
    MediaCard(
        cardColor = com.mymediashelf.app.ui.theme.CardBackgroundSecondary,
        borderColor = com.mymediashelf.app.ui.theme.CardBorderSecondary
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF000000)
                )
                val context = LocalContext.current
                Text(
                    text = "${item.type.name.lowercase()} • ${item.year ?: context.getString(R.string.empty)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1F2937)
                )
            }
            val context = LocalContext.current
            OutlinedButton(
                onClick = onRemove,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ){
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(context.getString(R.string.lists_remove_item))
            }
        }
    }
}