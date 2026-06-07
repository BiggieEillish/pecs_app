package com.wings.picexchange.data.local

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Plain Application avoids initialising the Hilt application; these tests build their own DB.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CardDaoTest {

    private lateinit var db: PicExchangeDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PicExchangeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    private fun newCategory(name: String) =
        CategoryEntity(name = name, imageRef = "drawable:category_starters", position = 0)

    @Test
    fun insertsAndObservesCardsInPositionOrder() = runBlocking {
        val categoryId = db.categoryDao().insert(newCategory("Food"))
        db.cardDao().insert(card("water", categoryId, position = 1))
        db.cardDao().insert(card("apple", categoryId, position = 0))

        val cards = db.cardDao().observeByCategory(categoryId).first()

        assertThat(cards.map { it.label }).containsExactly("apple", "water").inOrder()
    }

    @Test
    fun deletingCategoryCascadesToItsCards() = runBlocking {
        val categoryId = db.categoryDao().insert(newCategory("Play"))
        db.cardDao().insert(card("ball", categoryId, position = 0))
        assertThat(db.cardDao().countInCategory(categoryId)).isEqualTo(1)

        val category = db.categoryDao().getById(categoryId)!!
        db.categoryDao().delete(category)

        assertThat(db.cardDao().count()).isEqualTo(0)
    }

    @Test
    fun persistsCardTypeViaConverter() = runBlocking {
        val categoryId = db.categoryDao().insert(newCategory("Starters"))
        db.cardDao().insert(
            CardEntity(
                label = "I want", type = CardType.SENTENCE_STARTER,
                imageRef = "drawable:starter_want", categoryId = categoryId, position = 0,
            ),
        )

        val stored = db.cardDao().observeByCategory(categoryId).first().single()

        assertThat(stored.type).isEqualTo(CardType.SENTENCE_STARTER)
    }

    @Test
    fun cascadeDeleteRemovesOnlyTheDeletedCategorysCards() = runBlocking<Unit> {
        val keepId = db.categoryDao().insert(newCategory("Keep"))
        val dropId = db.categoryDao().insert(newCategory("Drop"))
        db.cardDao().insert(card("keep1", keepId, position = 0))
        db.cardDao().insert(card("drop1", dropId, position = 0))
        db.cardDao().insert(card("drop2", dropId, position = 1))

        db.categoryDao().delete(db.categoryDao().getById(dropId)!!)

        assertThat(db.cardDao().countInCategory(dropId)).isEqualTo(0)
        assertThat(db.cardDao().observeByCategory(keepId).first().map { it.label })
            .containsExactly("keep1")
    }

    private fun card(label: String, categoryId: Long, position: Int) =
        CardEntity(
            label = label, type = CardType.PICTURE,
            imageRef = "drawable:card_$label", categoryId = categoryId, position = position,
        )
}
