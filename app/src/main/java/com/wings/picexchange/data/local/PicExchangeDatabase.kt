package com.wings.picexchange.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wings.picexchange.data.local.dao.CardDao
import com.wings.picexchange.data.local.dao.CategoryDao
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.data.local.entity.CategoryEntity

@Database(
    entities = [CategoryEntity::class, CardEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PicExchangeDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun cardDao(): CardDao

    companion object {
        const val NAME = "picexchange.db"
    }
}
