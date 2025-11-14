package com.mymediashelf.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.ItemStatus
import com.mymediashelf.app.domain.model.ItemType

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String,
    val year: Int? = null,
    val status: String,
    val favorite: Int = 0,
    val rating: Int? = null,
    val comment: String? = null
){
    fun toDomain(): Item{
        return Item(
            id = id,
            title = title,
            type = ItemType.valueOf(type.uppercase()),
            year = year,
            status = ItemStatus.valueOf(status.uppercase()),
            favorite = favorite == 1,
            rating = rating,
            comment = comment
        )
    }

    companion object {
        fun fromDomain(item: Item): ItemEntity {
            return ItemEntity(
                id = item.id,
                title = item.title,
                type = item.type.name.lowercase(),
                year = item.year,
                status = item.status.name.lowercase(),
                favorite = if (item.favorite) 1 else 0,
                rating = item.rating,
                comment = item.comment
            )
        }
    }
}