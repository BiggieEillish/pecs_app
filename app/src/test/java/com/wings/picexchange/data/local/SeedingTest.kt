package com.wings.picexchange.data.local

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SeedingTest {

    private lateinit var db: PicExchangeDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PicExchangeDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(SeedCallback())
            .build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun seedsAllCategoriesAndCardsOnCreate() = runBlocking {
        val categories = db.categoryDao().observeAll().first()
        assertThat(categories).hasSize(SeedData.categories.size)
        assertThat(db.cardDao().count()).isEqualTo(SeedData.cards.size)
    }

    @Test
    fun startersCategoryContainsOnlySentenceStarters() = runBlocking {
        val starters = db.cardDao().observeByCategory(SeedData.STARTERS_CATEGORY_ID).first()
        assertThat(starters).isNotEmpty()
        assertThat(starters.all { it.type == CardType.SENTENCE_STARTER }).isTrue()
    }

    @Test
    fun eachCategoryStoresItsSeededCards() = runBlocking {
        val expectedPerCategory = SeedData.cards.groupBy { it.categoryId }
        expectedPerCategory.forEach { (categoryId, expected) ->
            val stored = db.cardDao().observeByCategory(categoryId).first()
            assertThat(stored.map { it.label })
                .containsExactlyElementsIn(expected.map { it.label })
        }
    }
}
