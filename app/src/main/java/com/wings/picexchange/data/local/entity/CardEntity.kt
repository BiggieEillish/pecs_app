package com.wings.picexchange.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wings.picexchange.data.local.CardType

/**
 * A single picture or sentence-starter card belonging to a category. Deleting the parent
 * category cascades to its cards (ForeignKey.CASCADE).
 */
@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId")],
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val type: CardType,
    /** Typed image reference: "drawable:<name>" (built-in) or "file:<path>" (custom). */
    val imageRef: String,
    val categoryId: Long,
    /** Stable ordering within the card library. */
    val position: Int,
    /** True for cards seeded with the app (vs. user-created). */
    val isBuiltIn: Boolean = false,
)
