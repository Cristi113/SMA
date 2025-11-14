package com.mymediashelf.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.mymediashelf.app.domain.model.Item
import com.mymediashelf.app.domain.model.MediaList

data class ListWithItems(
    @Embedded
    val list: ListEntity,

    @Relation(
        entity = ItemEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = ListItemCrossRef::class,
            parentColumn = "list_id",
            entityColumn = "item_id"
        )
    )
    val items: List<ItemEntity>
) {
    fun toDomain(): MediaList {
        return MediaList(
            id = list.id,
            name = list.name,
            items = items.map { it.toDomain() }
        )
    }
}