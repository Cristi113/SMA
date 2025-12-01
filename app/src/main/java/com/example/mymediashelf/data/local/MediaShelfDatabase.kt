package com.mymediashelf.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mymediashelf.app.data.local.dao.*
import com.mymediashelf.app.data.local.entity.*

@Database(
    entities = [
        ItemEntity::class,
        TagEntity::class,
        ItemTagCrossRef::class,
        ListEntity::class,
        ListItemCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MediaShelfDatabase : RoomDatabase(){
    abstract fun itemDao(): ItemDao
    abstract fun tagDao(): TagDao
    abstract fun itemTagDao(): ItemTagDao
    abstract fun listDao(): ListDao
    abstract fun listItemDao(): ListItemDao

    companion object{
        const val DATABASE_NAME = "media_shelf_db"
    }
}