package com.wings.picexchange.data.local

import androidx.room.TypeConverter

/** Room type converters. CardType is persisted as its enum name (stable, human-readable). */
class Converters {
    @TypeConverter
    fun cardTypeToString(type: CardType): String = type.name

    @TypeConverter
    fun stringToCardType(value: String): CardType = CardType.valueOf(value)
}
