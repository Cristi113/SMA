package com.mymediashelf.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymediashelf.app.domain.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
) {
    fun toDomain(): Tag {
        return Tag(id = id, name = name)
    }

    companion object {
        fun fromDomain(tag: Tag): TagEntity {
            return TagEntity(id = tag.id, name = tag.name)
        }
    }
}