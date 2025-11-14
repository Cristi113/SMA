package com.mymediashelf.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymediashelf.app.domain.model.MediaList

@Entity(tableName = "lists")
data class ListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
) {
    fun toDomain(): MediaList {
        return MediaList(id = id, name = name)
    }

    companion object {
        fun fromDomain(list: MediaList): ListEntity {
            return ListEntity(id = list.id, name = list.name)
        }
    }
}