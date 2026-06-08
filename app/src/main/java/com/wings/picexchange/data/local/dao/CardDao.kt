package com.wings.picexchange.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wings.picexchange.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

/** Reads are reactive (Flow); writes are suspend functions. */
@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE categoryId = :categoryId ORDER BY position ASC, id ASC")
    fun observeByCategory(categoryId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM cards WHERE categoryId = :categoryId")
    suspend fun countInCategory(categoryId: Long): Int

    @Query("SELECT * FROM cards WHERE categoryId = :categoryId ORDER BY position ASC, id ASC")
    suspend fun getByCategory(categoryId: Long): List<CardEntity>

    @Query("SELECT COALESCE(MAX(position), -1) FROM cards WHERE categoryId = :categoryId")
    suspend fun maxPosition(categoryId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(cards: List<CardEntity>): List<Long>

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)
}
