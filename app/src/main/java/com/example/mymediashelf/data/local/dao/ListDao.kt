package com.mymediashelf.app.data.local.dao

import androidx.room.*
import com.mymediashelf.app.data.local.entity.ListEntity
import com.mymediashelf.app.data.local.entity.ListWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface ListDao{
    @Query("SELECT * FROM lists ORDER BY id DESC")
    fun getAllListsFlow(): Flow<List<ListEntity>>

    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun getListById(id: Long): ListEntity?

    @Transaction
    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun getListWithItemsById(id: Long): ListWithItems?

    @Transaction
    @Query("SELECT * FROM lists ORDER BY id DESC")
    fun getAllListsWithItemsFlow(): Flow<List<ListWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity): Long

    @Update
    suspend fun updateList(list: ListEntity)

    @Delete
    suspend fun deleteList(list: ListEntity)

    @Query("DELETE FROM lists WHERE id = :id")
    suspend fun deleteListById(id: Long)

    @Query("SELECT COUNT(*) FROM lists")
    suspend fun getListCount(): Int
}