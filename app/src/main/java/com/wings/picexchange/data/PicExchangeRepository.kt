package com.wings.picexchange.data

import com.wings.picexchange.data.local.dao.CardDao
import com.wings.picexchange.data.local.dao.CategoryDao
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single point of access to persisted cards/categories for the UI layer. Reads are exposed
 * as Flows; writes are suspend functions. Grows with each milestone (M5 adds full CRUD).
 */
@Singleton
class PicExchangeRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val cardDao: CardDao,
) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeCards(categoryId: Long): Flow<List<CardEntity>> =
        cardDao.observeByCategory(categoryId)

    suspend fun getCategory(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun getCard(id: Long): CardEntity? = cardDao.getById(id)

    suspend fun cardsIn(categoryId: Long): List<CardEntity> = cardDao.getByCategory(categoryId)

    suspend fun nextCardPosition(categoryId: Long): Int = cardDao.maxPosition(categoryId) + 1

    suspend fun addCard(card: CardEntity): Long = cardDao.insert(card)

    suspend fun updateCard(card: CardEntity) = cardDao.update(card)

    suspend fun deleteCard(card: CardEntity) = cardDao.delete(card)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun nextCategoryPosition(): Int = categoryDao.maxPosition() + 1
}
