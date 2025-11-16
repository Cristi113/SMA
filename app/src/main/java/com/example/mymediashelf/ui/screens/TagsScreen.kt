package com.mymediashelf.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mymediashelf.app.ui.theme.GradientTagsEnd
import com.mymediashelf.app.ui.theme.GradientTagsMiddle
import com.mymediashelf.app.ui.theme.GradientTagsStart
import com.mymediashelf.app.R
import com.mymediashelf.app.domain.model.Tag
import com.mymediashelf.app.ui.components.*
import com.mymediashelf.app.ui.utils.rememberShakeDetector
import com.mymediashelf.app.ui.viewmodel.TagsViewModel
import com.mymediashelf.app.ui.viewmodel.viewModelWithFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    viewModel: TagsViewModel = viewModelWithFactory()
){
    val uiState by viewModel.uiState.collectAsState()
    val error = uiState.error

    rememberShakeDetector(
        onShake ={
            if(uiState.selectedTag != null){
                viewModel.getRandomItemFromSelectedTag()
            }
        }
    )

    Scaffold(
        topBar = {
            val context = LocalContext.current
            TopAppBar(
                title = { Text(context.getString(R.string.tags_title)) }
            )
        },
        containerColor = Color.Transparent
    ){ padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GradientTagsStart,
                            GradientTagsMiddle,
                            GradientTagsEnd
                        )
                    )
                )
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                AddTagSection(
                    onAddTag = { name ->
                        viewModel.addTag(Tag(name = name.trim()))
                    }
                )

                if (uiState.isLoading && uiState.tags.isEmpty()){
                    LoadingIndicator()
                }else if (uiState.tags.isEmpty()){
                    val context = LocalContext.current
                    EmptyState(
                        icon = Icons.Default.Label,
                        title = context.getString(R.string.tags_no_tags)
                    )
                }else{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        items(uiState.tags) { tag ->
                            TagCard(
                                tag = tag,
                                isSelected = uiState.selectedTag?.id == tag.id,
                                onSelect = { viewModel.selectTag(tag) },
                                onUpdate = viewModel::updateTag,
                                onDelete = viewModel::deleteTag
                            )
                        }
                    }
                }

                if (error != null){
                    ErrorState(
                        message = error,
                        onRetry = { viewModel.clearError() }
                    )
                }
            }
        }
    }

    if (uiState.showRandomDialog){
        RandomItemDialog(
            item = uiState.randomItem,
            onDismiss = { viewModel.dismissRandomDialog() }
        )
    }
}

@Composable
fun AddTagSection(
    onAddTag: (String) -> Unit
){
    var tagName by remember { mutableStateOf("") }

    MediaCard(
        cardColor = com.mymediashelf.app.ui.theme.CardBackgroundPrimary,
        borderColor = com.mymediashelf.app.ui.theme.CardBorderPrimary
    ){
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            val context = LocalContext.current
            Text(
                text = context.getString(R.string.tags_add_new),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            OutlinedTextField(
                value = tagName,
                onValueChange = { tagName = it },
                label = { Text(context.getString(R.string.tags_name_label), color = Color(0xFF1F2937)) },
                placeholder = { Text(context.getString(R.string.tags_name_hint), color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Label, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF000000),
                    unfocusedTextColor = Color(0xFF1F2937),
                    focusedLabelColor = Color(0xFF1F2937),
                    unfocusedLabelColor = Color(0xFF6B7280)
                )
            )
            Button(
                onClick = {
                    if(tagName.isNotBlank()){
                        onAddTag(tagName)
                        tagName = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A8A),
                    contentColor = Color(0xFFFFFFFF)
                )
            ){
                val context = LocalContext.current
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFFFFF))
                Spacer(Modifier.width(8.dp))
                Text(context.getString(R.string.tags_add_button), color = Color(0xFFFFFFFF))
            }
        }
    }
}

@Composable
fun TagCard(
    tag: Tag,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onUpdate: (Tag) -> Unit,
    onDelete: (Tag) -> Unit
){
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(tag.name) }

    MediaCard(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        cardColor = if(isSelected){
            com.mymediashelf.app.ui.theme.CardBackgroundPrimary
        }else{
            com.mymediashelf.app.ui.theme.CardBackgroundSecondary
        },
        borderColor = if (isSelected){
            com.mymediashelf.app.ui.theme.Primary
        }else{
            com.mymediashelf.app.ui.theme.Secondary
        }
    ){
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){
            if(isEditing){
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = {
                        val context = LocalContext.current
                        Text(context.getString(R.string.tags_name_label), color = Color(0xFF1F2937))
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
                ){
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            onUpdate(tag.copy(name = editedName.trim()))
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ){
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFFFFF))
                        Spacer(Modifier.width(4.dp))
                        Text(context.getString(R.string.tags_save), color = Color(0xFFFFFFFF))
                    }
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1E3A8A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E3A8A))
                    ){
                        Text(context.getString(R.string.cancel), color = Color(0xFF1E3A8A))
                    }
                }
            }else{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = tag.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ){
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFFFFF))
                        Spacer(Modifier.width(4.dp))
                        val context = LocalContext.current
                        Text(context.getString(R.string.edit), color = Color(0xFFFFFFFF))
                    }
                    OutlinedButton(
                        onClick = { onDelete(tag) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                    ) {
                        val context = LocalContext.current
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        Text(context.getString(R.string.tags_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}