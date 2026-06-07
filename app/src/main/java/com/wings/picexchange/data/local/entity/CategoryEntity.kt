package com.wings.picexchange.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A group of cards shown as a tile on the Home grid (e.g. "Food & Drink"). */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Typed image reference for the tile: "drawable:<name>" or "file:<path>". */
    val imageRef: String,
    /** Stable ordering within the Home grid. */
    val position: Int,
    /** True for categories seeded with the app (vs. user-created). */
    val isBuiltIn: Boolean = false,
)
