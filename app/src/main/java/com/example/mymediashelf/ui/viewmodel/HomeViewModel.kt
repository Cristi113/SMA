package com.mymediashelf.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymediashelf.app.data.repository.ItemRepository
import com.mymediashelf.app.data.repository.ListRepository
import com.mymediashelf.app.data.repository.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val itemCount: Int = 0,
    val tagCount: Int = 0,
    val listCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val itemRepository: ItemRepository,
    private val tagRepository: TagRepository,
    private val listRepository: ListRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init{
        loadStats()
    }

    private fun loadStats(){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                val itemCount = itemRepository.getItemCount()
                val tagCount = tagRepository.getTagCount()
                val listCount = listRepository.getListCount()

                _uiState.value = _uiState.value.copy(
                    itemCount = itemCount,
                    tagCount = tagCount,
                    listCount = listCount,
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

    fun refresh() {
        loadStats()
    }
}