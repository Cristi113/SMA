package com.mymediashelf.app.data.repository

import com.mymediashelf.app.data.local.dao.ItemDao
import com.mymediashelf.app.data.local.dao.ItemTagDao
import com.mymediashelf.app.data.local.entity.ItemEntity
import com.mymediashelf.app.data.local.entity.ItemTagCrossRef
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.ItemStatus
import com.mymediashelf.app.domain.model.ItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ItemRepository(
    private val itemDao: ItemDao,
    private val itemTagDao: ItemTagDao
) {
    fun getAllItems(): Flow<List<Item>> {
        return flow {
            itemDao.getAllItemsFlow().collect { entities ->
                val items = entities.map { entity ->
                    val tags = itemTagDao.getTagsForItem(entity.id)
                    entity.toDomain().copy(tags = tags.map { it.toDomain() })
                }
                emit(items)
            }
        }
    }

    fun getFilteredItems(
        query: String? = null,
        type: ItemType? = null,
        favorite: Boolean? = null,
        status: ItemStatus? = null,
        year: Int? = null
    ): Flow<List<Item>> {
        return flow {
            itemDao.getFilteredItemsFlow(
                query = query?.takeIf { it.isNotBlank() },
                type = type?.name?.lowercase(),
                favorite = favorite?.let { if (it) 1 else 0 },
                status = status?.name?.lowercase(),
                year = year
            ).collect { entities ->
                val items = entities.map { entity ->
                    val tags = itemTagDao.getTagsForItem(entity.id)
                    entity.toDomain().copy(tags = tags.map { it.toDomain() })
                }
                emit(items)
            }
        }
    }

    suspend fun getFilteredItemsByTag(
        tagId: Long,
        query: String? = null,
        type: ItemType? = null,
        favorite: Boolean? = null,
        status: ItemStatus? = null,
        year: Int? = null
    ): List<Item> {
        val itemsByTag = getItemsByTag(tagId)
        return itemsByTag.filter { item ->
            (query == null || query.isBlank() || item.title.contains(query, ignoreCase = true)) &&
                    (type == null || item.type == type) &&
                    (favorite == null || item.favorite == favorite) &&
                    (status == null || item.status == status) &&
                    (year == null || item.year == year)
        }
    }

    suspend fun getItemById(id: Long): Item? {
        val entity = itemDao.getItemById(id) ?: return null
        val tags = itemTagDao.getTagsForItem(id)
        return entity.toDomain().copy(tags = tags.map { it.toDomain() })
    }

    suspend fun insertItem(item: Item): Long {
        val entity = ItemEntity.fromDomain(item)
        val itemId = itemDao.insertItem(entity)

        item.tags.forEach { tag ->
            itemTagDao.insertItemTag(ItemTagCrossRef(itemId, tag.id))
        }

        return itemId
    }

    suspend fun updateItem(item: Item) {
        val entity = ItemEntity.fromDomain(item)
        itemDao.updateItem(entity)

        itemTagDao.deleteAllTagsForItem(item.id)
        item.tags.forEach { tag ->
            itemTagDao.insertItemTag(ItemTagCrossRef(item.id, tag.id))
        }
    }

    suspend fun deleteItem(item: Item) {
        itemDao.deleteItem(ItemEntity.fromDomain(item))
    }

    suspend fun deleteItemById(id: Long) {
        itemDao.deleteItemById(id)
    }

    suspend fun addTagToItem(itemId: Long, tagId: Long) {
        itemTagDao.insertItemTag(ItemTagCrossRef(itemId, tagId))
        val entity = itemDao.getItemById(itemId)
        entity?.let { itemDao.updateItem(it) }
    }

    suspend fun removeTagFromItem(itemId: Long, tagId: Long) {
        itemTagDao.deleteItemTag(itemId, tagId)
        val entity = itemDao.getItemById(itemId)
        entity?.let { itemDao.updateItem(it) }
    }


    suspend fun getItemCount(): Int {
        return itemDao.getItemCount()
    }

    suspend fun getItemsByTag(tagId: Long): List<Item> {
        val entities = itemTagDao.getItemsForTag(tagId)
        return entities.map { entity ->
            val tags = itemTagDao.getTagsForItem(entity.id)
            entity.toDomain().copy(tags = tags.map { it.toDomain() })
        }
    }

    suspend fun getRandomItem(items: List<Item>): Item? {
        return if (items.isNotEmpty()) {
            items.random()
        } else {
            null
        }
    }
}