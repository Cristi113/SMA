package com.mymediashelf.app.data.local.dao

import androidx.room.*
import com.mymediashelf.app.data.local.entity.ItemTagCrossRef
import com.mymediashelf.app.data.local.entity.TagEntity

@Dao
interface ItemTagDao{
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN item_tags it ON it.tag_id = t.id
        WHERE it.item_id = :itemId
    """)
    suspend fun getTagsForItem(itemId: Long): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemTag(crossRef: ItemTagCrossRef)

    @Delete
    suspend fun deleteItemTag(crossRef: ItemTagCrossRef)

    @Query("DELETE FROM item_tags WHERE item_id = :itemId AND tag_id = :tagId")
    suspend fun deleteItemTag(itemId: Long, tagId: Long)

    @Query("DELETE FROM item_tags WHERE item_id = :itemId")
    suspend fun deleteAllTagsForItem(itemId: Long)

    @Query("""
        SELECT i.* FROM items i
        INNER JOIN item_tags it ON it.item_id = i.id
        WHERE it.tag_id = :tagId
        ORDER BY i.id DESC
    """)
    suspend fun getItemsForTag(tagId: Long): List<com.mymediashelf.app.data.local.entity.ItemEntity>
}