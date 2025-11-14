package com.mymediashelf.app.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider{
    private var database: MediaShelfDatabase? = null

    fun getDatabase(context: Context): MediaShelfDatabase{
        if(database == null){
            database = Room.databaseBuilder(
                context.applicationContext,
                MediaShelfDatabase::class.java,
                MediaShelfDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration(true)
                .build()
        }
        return database!!
    }
}