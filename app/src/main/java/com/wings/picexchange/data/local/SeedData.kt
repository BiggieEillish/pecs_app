package com.wings.picexchange.data.local

import com.wings.picexchange.data.local.entity.CardEntity
import com.wings.picexchange.data.local.entity.CategoryEntity

/**
 * Default content inserted on first run (see [SeedCallback]). Explicit ids are used so the
 * cards can reference their parent categories deterministically.
 *
 * NOTE: image refs point at PLACEHOLDER drawables for now — the real Wings Melaka categories,
 * labels (Malay/English), and per-card icons are pending sign-off (architecture open #5/#6).
 */
object SeedData {

    const val STARTERS_CATEGORY_ID = 1L

    val categories: List<CategoryEntity> = listOf(
        CategoryEntity(id = STARTERS_CATEGORY_ID, name = "Starters", imageRef = "drawable:category_starters", position = 0, isBuiltIn = true),
        CategoryEntity(id = 2, name = "Food & Drink", imageRef = "drawable:category_food", position = 1, isBuiltIn = true),
        CategoryEntity(id = 3, name = "Play", imageRef = "drawable:category_play", position = 2, isBuiltIn = true),
        CategoryEntity(id = 4, name = "Feelings", imageRef = "drawable:category_feelings", position = 3, isBuiltIn = true),
    )

    val cards: List<CardEntity> = listOf(
        // Sentence starters
        starter(1, "I want", "starter_want", 0),
        starter(2, "I see", "starter_see", 1),
        starter(3, "I feel", "starter_feel", 2),
        starter(4, "I need", "starter_need", 3),
        // Food & Drink (category 2)
        picture(5, "water", "card_water", 2, 0),
        picture(6, "apple", "card_apple", 2, 1),
        picture(7, "milk", "card_milk", 2, 2),
        picture(8, "cookie", "card_cookie", 2, 3),
        // Play (category 3)
        picture(9, "ball", "card_ball", 3, 0),
        picture(10, "book", "card_book", 3, 1),
        picture(11, "blocks", "card_blocks", 3, 2),
        // Feelings (category 4)
        picture(12, "happy", "card_happy", 4, 0),
        picture(13, "sad", "card_sad", 4, 1),
        picture(14, "tired", "card_tired", 4, 2),
    )

    private fun starter(id: Long, label: String, drawable: String, position: Int) =
        CardEntity(
            id = id, label = label, type = CardType.SENTENCE_STARTER,
            imageRef = "drawable:$drawable", categoryId = STARTERS_CATEGORY_ID,
            position = position, isBuiltIn = true,
        )

    private fun picture(id: Long, label: String, drawable: String, categoryId: Long, position: Int) =
        CardEntity(
            id = id, label = label, type = CardType.PICTURE,
            imageRef = "drawable:$drawable", categoryId = categoryId,
            position = position, isBuiltIn = true,
        )
}
