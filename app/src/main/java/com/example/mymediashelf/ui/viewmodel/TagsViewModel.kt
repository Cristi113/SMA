package com.mymediashelf.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymediashelf.app.data.repository.ItemRepository
import com.mymediashelf.app.data.repository.TagRepository
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TagsUiState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTag: Tag? = null,
    val randomItem: Item? = null,
    val showRandomDialog: Boolean = false
)

class TagsViewModel(
    private val tagRepository: TagRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    private var lastShakeTime = 0L
    private val SHAKE_DEBOUNCE_MS = 2000L

    init{
        loadTags()
    }

    private fun loadTags(){
        viewModelScope.launch{
            tagRepository.getAllTags().collect { tags ->
                _uiState.value = _uiState.value.copy(tags = tags, isLoading = false)
            }
        }
    }

    fun addTag(tag: Tag){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                val result = tagRepository.insertTag(tag)
                result.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Tag already exists"
                    )
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error adding tag"
                )
            }
        }
    }

    fun updateTag(tag: Tag){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                tagRepository.updateTag(tag)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteTag(tag: Tag){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                tagRepository.deleteTag(tag)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError(){
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun selectTag(tag: Tag){
        _uiState.value = _uiState.value.copy(selectedTag = tag)
    }

    fun getRandomItemFromSelectedTag(){
        viewModelScope.launch{
            val state = _uiState.value

            val currentTime = System.currentTimeMillis()
            if (state.showRandomDialog || (currentTime - lastShakeTime) < SHAKE_DEBOUNCE_MS) {
                return@launch
            }

            val selectedTag = state.selectedTag
            if(selectedTag != null){
                val items = itemRepository.getItemsByTag(selectedTag.id)
                if(items.isNotEmpty()){
                    val random = itemRepository.getRandomItem(items)
                    if(random != null){
                        lastShakeTime = currentTime
                        _uiState.value = _uiState.value.copy(
                            randomItem = random,
                            showRandomDialog = true
                        )
                    }
                }
            }
        }
    }

    fun dismissRandomDialog(){
        _uiState.value = _uiState.value.copy(showRandomDialog = false, randomItem = null)
        lastShakeTime = System.currentTimeMillis()
    }
}