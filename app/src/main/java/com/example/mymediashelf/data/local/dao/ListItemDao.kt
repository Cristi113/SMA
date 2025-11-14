package com.mymediashelf.app.data.local.dao

import androidx.room.*
import com.mymediashelf.app.data.local.entity.ItemEntity
import com.mymediashelf.app.data.local.entity.ListItemCrossRef

@Dao
interface ListItemDao{
    @Query("""
        SELECT i.* FROM items i
        INNER JOIN list_items li ON li.item_id = i.id
        WHERE li.list_id = :listId
        ORDER BY i.id DESC
    """)
    suspend fun getItemsForList(listId: Long): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListItem(crossRef: ListItemCrossRef)

    @Delete
    suspend fun deleteListItem(crossRef: ListItemCrossRef)

    @Query("DELETE FROM list_items WHERE list_id = :listId AND item_id = :itemId")
    suspend fun deleteListItem(listId: Long, itemId: Long)

    @Query("DELETE FROM list_items WHERE list_id = :listId")
    suspend fun deleteAllItemsForList(listId: Long)

    @Transaction
    suspend fun updateListItems(listId: Long, itemIds: List<Long>) {
        deleteAllItemsForList(listId)

        itemIds.forEach { itemId ->
            insertListItem(ListItemCrossRef(listId, itemId))
        }
    }
}