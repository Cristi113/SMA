package com.mymediashelf.app.data.repository

import com.mymediashelf.app.data.local.dao.TagDao
import com.mymediashelf.app.data.local.entity.TagEntity
import com.mymediashelf.app.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepository(
    private val tagDao: TagDao
) {
    fun getAllTags(): Flow<List<Tag>>{
        return tagDao.getAllTagsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTagById(id: Long): Tag?{
        return tagDao.getTagById(id)?.toDomain()
    }

    suspend fun getTagByName(name: String): Tag?{
        return tagDao.getTagByName(name)?.toDomain()
    }

    suspend fun insertTag(tag: Tag): Result<Long>{
        return try{
            val entity = TagEntity.fromDomain(tag)
            val id = tagDao.insertTag(entity)
            if (id > 0){
                Result.success(id)
            }else{
                val existing = tagDao.getTagByName(tag.name)
                Result.failure(Exception("Tag already exists"))
            }
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun updateTag(tag: Tag){
        val entity = TagEntity.fromDomain(tag)
        tagDao.updateTag(entity)
    }

    suspend fun deleteTag(tag: Tag){
        tagDao.deleteTag(TagEntity.fromDomain(tag))
    }

    suspend fun deleteTagById(id: Long){
        tagDao.deleteTagById(id)
    }

    suspend fun getTagCount(): Int{
        return tagDao.getTagCount()
    }
}