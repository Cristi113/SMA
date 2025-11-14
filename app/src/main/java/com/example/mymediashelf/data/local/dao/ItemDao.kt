package com.mymediashelf.app.data.local.dao

import androidx.room.*
import com.mymediashelf.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao{
    @Query("SELECT * FROM items ORDER BY id DESC")
    fun getAllItemsFlow(): Flow<List<ItemEntity>>

    @Query("""
        SELECT * FROM items 
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%')
        AND (:type IS NULL OR type = :type)
        AND (:favorite IS NULL OR favorite = :favorite)
        AND (:status IS NULL OR status = :status)
        AND (:year IS NULL OR year = :year)
        ORDER BY id DESC
    """)
    fun getFilteredItemsFlow(
        query: String? = null,
        type: String? = null,
        favorite: Int? = null,
        status: String? = null,
        year: Int? = null
    ): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int
}