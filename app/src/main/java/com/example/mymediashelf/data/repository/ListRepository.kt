package com.mymediashelf.app.data.repository

import com.mymediashelf.app.data.local.dao.ItemTagDao
import com.mymediashelf.app.data.local.dao.ListDao
import com.mymediashelf.app.data.local.dao.ListItemDao
import com.mymediashelf.app.data.local.entity.ListEntity
import com.mymediashelf.app.data.local.entity.ListItemCrossRef
import com.mymediashelf.app.data.local.entity.ListWithItems
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.MediaList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ListRepository(
    private val listDao: ListDao,
    private val listItemDao: ListItemDao,
    private val itemTagDao: ItemTagDao
) {

    fun getAllLists(): Flow<List<MediaList>>{
        return listDao.getAllListsWithItemsFlow().map { listWithItemsList ->
            listWithItemsList.map { listWithItems ->
                val itemsWithTags = listWithItems.items.map { itemEntity ->
                    val tags = itemTagDao.getTagsForItem(itemEntity.id)
                    itemEntity.toDomain().copy(tags = tags.map { it.toDomain() })
                }
                listWithItems.list.toDomain().copy(items = itemsWithTags)
            }
        }
    }

    suspend fun getListById(id: Long): MediaList?{
        val listWithItems = listDao.getListWithItemsById(id) ?: return null
        val itemsWithTags = listWithItems.items.map { itemEntity ->
            val tags = itemTagDao.getTagsForItem(itemEntity.id)
            itemEntity.toDomain().copy(tags = tags.map { it.toDomain() })
        }
        return listWithItems.list.toDomain().copy(items = itemsWithTags)
    }

    suspend fun insertList(list: MediaList): Long {
        val entity = ListEntity.fromDomain(list)
        val listId = listDao.insertList(entity)

        list.items.forEach { item ->
            listItemDao.insertListItem(ListItemCrossRef(listId, item.id))
        }
        return listId
    }

    suspend fun updateListItems(listId: Long, itemIds: List<Long>) {
        listItemDao.updateListItems(listId, itemIds)
    }

    suspend fun updateList(list: MediaList) {
        val entity = ListEntity.fromDomain(list)
        listDao.updateList(entity)
    }

    suspend fun deleteList(list: MediaList) {
        listDao.deleteList(ListEntity.fromDomain(list))
    }

    suspend fun deleteListById(id: Long) {
        listDao.deleteListById(id)
    }

    suspend fun getItemsForList(listId: Long): List<Item> {
        val items = listItemDao.getItemsForList(listId)
        return items.map { entity ->
            val tags = itemTagDao.getTagsForItem(entity.id)
            entity.toDomain().copy(tags = tags.map { it.toDomain() })
        }
    }

    suspend fun addItemToList(listId: Long, itemId: Long) {
        listItemDao.insertListItem(ListItemCrossRef(listId, itemId))
    }

    suspend fun removeItemFromList(listId: Long, itemId: Long) {
        listItemDao.deleteListItem(listId, itemId)
    }

    suspend fun getListCount(): Int {
        return listDao.getListCount()
    }
}