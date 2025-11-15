package com.mymediashelf.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mymediashelf.app.data.repository.ItemRepository
import com.mymediashelf.app.data.repository.ListRepository
import com.mymediashelf.app.data.repository.TagRepository

class ViewModelFactory(
    private val itemRepository: ItemRepository,
    private val tagRepository: TagRepository,
    private val listRepository: ListRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(itemRepository, tagRepository, listRepository) as T
            }
            modelClass.isAssignableFrom(ItemsViewModel::class.java) -> {
                ItemsViewModel(itemRepository, tagRepository, listRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}