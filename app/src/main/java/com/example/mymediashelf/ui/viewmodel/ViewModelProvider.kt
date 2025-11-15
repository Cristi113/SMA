package com.mymediashelf.app.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mymediashelf.app.data.local.DatabaseProvider
import com.mymediashelf.app.data.repository.ItemRepository
import com.mymediashelf.app.data.repository.ListRepository
import com.mymediashelf.app.data.repository.TagRepository

@Composable
fun getViewModelFactory(): ViewModelFactory {
    val context = LocalContext.current.applicationContext

    return remember {
        val database = DatabaseProvider.getDatabase(context)

        val itemRepository = ItemRepository(
            itemDao = database.itemDao(),
            itemTagDao = database.itemTagDao()
        )

        val tagRepository = TagRepository(
            tagDao = database.tagDao()
        )

        val listRepository = ListRepository(
            listDao = database.listDao(),
            listItemDao = database.listItemDao(),
            itemTagDao = database.itemTagDao()
        )

        ViewModelFactory(
            itemRepository = itemRepository,
            tagRepository = tagRepository,
            listRepository = listRepository
        )
    }
}

@Composable
inline fun <reified T : ViewModel> viewModelWithFactory(): T {
    val factory = getViewModelFactory()
    return viewModel(factory = factory)
}