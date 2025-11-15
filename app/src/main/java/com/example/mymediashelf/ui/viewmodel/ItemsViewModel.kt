package com.mymediashelf.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymediashelf.app.data.repository.ItemRepository
import com.mymediashelf.app.data.repository.ListRepository
import com.mymediashelf.app.data.repository.TagRepository
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.ItemStatus
import com.mymediashelf.app.domain.model.ItemType
import com.mymediashelf.app.domain.model.MediaList
import com.mymediashelf.app.domain.model.Tag
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf

data class ItemsUiState(
    val items: List<Item> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val allLists: List<MediaList> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedType: ItemType? = null,
    val selectedStatus: ItemStatus? = null,
    val favoritesOnly: Boolean = false,
    val selectedYear: Int? = null,
    val selectedTagId: Long? = null,
    val selectedListId: Long? = null,
    val randomItem: Item? = null,
    val showRandomDialog: Boolean = false
)

class ItemsViewModel(
    private val itemRepository: ItemRepository,
    private val tagRepository: TagRepository,
    private val listRepository: ListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    private var loadItemsJob: Job? = null
    private var lastShakeTime = 0L
    private val SHAKE_DEBOUNCE_MS = 2000L

    init {
        loadTags()
        loadLists()
        loadItems()
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.value = _uiState.value.copy(allTags = tags)
            }
        }
    }

    private fun loadLists() {
        viewModelScope.launch {
            listRepository.getAllLists().collect { lists ->
                _uiState.value = _uiState.value.copy(allLists = lists)
            }
        }
    }

    private fun loadItems() {
        loadItemsJob?.cancel()
        loadItemsJob = viewModelScope.launch {
            val state = _uiState.value

            if (state.selectedListId != null) {
                val baseItems = listRepository.getItemsForList(state.selectedListId)

                val filteredItems = baseItems.filter { item ->
                    (state.searchQuery.isBlank() || item.title.contains(state.searchQuery, ignoreCase = true)) &&
                            (state.selectedType == null || item.type == state.selectedType) &&
                            (state.selectedStatus == null || item.status == state.selectedStatus) &&
                            (!state.favoritesOnly || item.favorite) &&
                            (state.selectedYear == null || item.year == state.selectedYear) &&
                            (state.selectedTagId == null || item.tags.any { it.id == state.selectedTagId })
                }

                _uiState.value = _uiState.value.copy(items = filteredItems, isLoading = false)
            } else {
                val allItemsFlow = if (state.selectedTagId != null) {
                    val filteredItems = itemRepository.getFilteredItemsByTag(
                        tagId = state.selectedTagId,
                        query = state.searchQuery.takeIf { it.isNotBlank() },
                        type = state.selectedType,
                        status = state.selectedStatus,
                        favorite = state.favoritesOnly.takeIf { it }?.let { true },
                        year = state.selectedYear
                    )
                    flowOf(filteredItems)
                } else {
                    itemRepository.getFilteredItems(
                        query = state.searchQuery.takeIf { it.isNotBlank() },
                        type = state.selectedType,
                        status = state.selectedStatus,
                        favorite = state.favoritesOnly.takeIf { it }?.let { true },
                        year = state.selectedYear
                    )
                }
                allItemsFlow.collect { items ->
                    _uiState.value = _uiState.value.copy(items = items, isLoading = false)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadItems()
    }

    fun updateTypeFilter(type: ItemType?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        loadItems()
    }

    fun updateStatusFilter(status: ItemStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        loadItems()
    }

    fun updateFavoritesOnly(favoritesOnly: Boolean) {
        _uiState.value = _uiState.value.copy(favoritesOnly = favoritesOnly)
        loadItems()
    }

    fun addItem(item: Item) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, showRandomDialog = false, randomItem = null)
            try {
                val tagsWithIds = item.tags.mapNotNull { tag ->
                    if (tag.id == 0L) {
                        val existingTag = tagRepository.getTagByName(tag.name)
                        if (existingTag != null) {
                            existingTag
                        } else {
                            val result = tagRepository.insertTag(tag)
                            result.fold(
                                onSuccess = { tagId ->
                                    tag.copy(id = tagId)
                                },
                                onFailure = {
                                    tagRepository.getTagByName(tag.name) ?: tag
                                }
                            )
                        }
                    } else {
                        tag
                    }
                }

                val itemWithTags = item.copy(tags = tagsWithIds)
                itemRepository.insertItem(itemWithTags)
                _uiState.value = _uiState.value.copy(isLoading = false, showRandomDialog = false, randomItem = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    showRandomDialog = false,
                    randomItem = null
                )
            }
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                itemRepository.updateItem(item)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                itemRepository.deleteItem(item)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addTagToItem(itemId: Long, tagId: Long) {
        viewModelScope.launch {
            try {
                itemRepository.addTagToItem(itemId, tagId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeTagFromItem(itemId: Long, tagId: Long) {
        viewModelScope.launch {
            try {
                itemRepository.removeTagFromItem(itemId, tagId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun updateYearFilter(year: Int?) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
        loadItems()
    }

    fun updateTagFilter(tagId: Long?) {
        _uiState.value = _uiState.value.copy(selectedTagId = tagId)
        loadItems()
    }

    fun updateListFilter(listId: Long?) {
        _uiState.value = _uiState.value.copy(selectedListId = listId)
        loadItems()
    }

    fun getRandomItem() {
        viewModelScope.launch {
            val state = _uiState.value

            val currentTime = System.currentTimeMillis()
            if (state.showRandomDialog || (currentTime - lastShakeTime) < SHAKE_DEBOUNCE_MS) {
                return@launch
            }

            val currentItems = state.items
            if (currentItems.isNotEmpty()) {
                val random = itemRepository.getRandomItem(currentItems)
                if (random != null) {
                    lastShakeTime = currentTime
                    _uiState.value = _uiState.value.copy(
                        randomItem = random,
                        showRandomDialog = true
                    )
                }
            }
        }
    }

    fun dismissRandomDialog() {
        _uiState.value = _uiState.value.copy(showRandomDialog = false, randomItem = null)
        lastShakeTime = System.currentTimeMillis()
    }

    fun createTag(tagName: String) {
        viewModelScope.launch {
            try {
                val tag = Tag(name = tagName.trim())
                tagRepository.insertTag(tag)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}