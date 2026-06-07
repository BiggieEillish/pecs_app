package com.wings.picexchange.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Inserts [SeedData] the first time the database is created. Uses synchronous parameterized
 * SQL inside onCreate so the seed rows are guaranteed present before any query returns
 * (no coroutine race), which also makes seeding deterministically testable.
 */
class SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        SeedData.categories.forEach { c ->
            db.execSQL(
                "INSERT INTO categories (id, name, imageRef, position, isBuiltIn) VALUES (?, ?, ?, ?, ?)",
                arrayOf(c.id, c.name, c.imageRef, c.position, c.isBuiltIn.toInt()),
            )
        }
        SeedData.cards.forEach { card ->
            db.execSQL(
                "INSERT INTO cards (id, label, type, imageRef, categoryId, position, isBuiltIn) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf(
                    card.id, card.label, card.type.name, card.imageRef,
                    card.categoryId, card.position, card.isBuiltIn.toInt(),
                ),
            )
        }
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0
}
