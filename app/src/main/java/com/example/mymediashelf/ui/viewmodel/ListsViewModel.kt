package com.mymediashelf.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymediashelf.app.data.repository.ItemRepository
import com.mymediashelf.app.data.repository.ListRepository
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.MediaList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListsUiState(
    val lists: List<MediaList> = emptyList(),
    val allItems: List<Item> = emptyList(),
    val selectedList: MediaList? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val randomItem: Item? = null,
    val showRandomDialog: Boolean = false
)

class ListsViewModel(
    private val listRepository: ListRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    private var lastShakeTime = 0L
    private val SHAKE_DEBOUNCE_MS = 2000L

    init{
        loadLists()
        loadAllItems()
    }

    private fun loadLists(){
        viewModelScope.launch{
            listRepository.getAllLists().collect { lists ->
                _uiState.value = _uiState.value.copy(lists = lists, isLoading = false)
            }
        }
    }

    private fun loadAllItems(){
        viewModelScope.launch{
            itemRepository.getAllItems().collect { items ->
                _uiState.value = _uiState.value.copy(allItems = items)
            }
        }
    }

    fun selectList(list: MediaList){
        viewModelScope.launch{
            val fullList = listRepository.getListById(list.id)
            _uiState.value = _uiState.value.copy(selectedList = fullList)
        }
    }

    fun createList(name: String, items: List<Item> = emptyList()) {
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                val newList = MediaList(name = name.trim(), items = items)
                val listId = listRepository.insertList(newList)
                loadLists()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateList(list: MediaList){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                listRepository.updateList(list)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteList(list: MediaList){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                listRepository.deleteList(list)
                if (_uiState.value.selectedList?.id == list.id) {
                    _uiState.value = _uiState.value.copy(selectedList = null)
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addItemToList(listId: Long, itemId: Long){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                listRepository.addItemToList(listId, itemId)
                val updatedList = listRepository.getListById(listId)
                _uiState.value = _uiState.value.copy(
                    selectedList = updatedList,
                    isLoading = false
                )
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun removeItemFromList(listId: Long, itemId: Long){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                listRepository.removeItemFromList(listId, itemId)
                val updatedList = listRepository.getListById(listId)
                _uiState.value = _uiState.value.copy(
                    selectedList = updatedList,
                    isLoading = false
                )
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateListItems(listId: Long, itemIds: List<Long>){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                listRepository.updateListItems(listId, itemIds)
                val updatedList = listRepository.getListById(listId)
                _uiState.value = _uiState.value.copy(
                    selectedList = updatedList,
                    isLoading = false
                )
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

    fun getRandomItemFromSelectedList(){
        viewModelScope.launch{
            val state = _uiState.value

            val currentTime = System.currentTimeMillis()
            if (state.showRandomDialog || (currentTime - lastShakeTime) < SHAKE_DEBOUNCE_MS){
                return@launch
            }

            val selectedList = state.selectedList
            if (selectedList != null && selectedList.items.isNotEmpty()){
                val random = itemRepository.getRandomItem(selectedList.items)
                if (random != null){
                    lastShakeTime = currentTime
                    _uiState.value = _uiState.value.copy(
                        randomItem = random,
                        showRandomDialog = true
                    )
                }
            }
        }
    }

    fun dismissRandomDialog(){
        _uiState.value = _uiState.value.copy(showRandomDialog = false, randomItem = null)
        lastShakeTime = System.currentTimeMillis()
    }
}