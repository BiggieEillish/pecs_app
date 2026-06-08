package com.wings.picexchange.ui.strip

import android.os.Parcelable
import com.wings.picexchange.data.local.CardType
import com.wings.picexchange.data.local.entity.CardEntity
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * One item placed on the sentence strip. It SNAPSHOTS the card's label + image at add-time, so
 * editing or deleting the source card later never corrupts an in-progress sentence. [instanceId]
 * is unique per placement (the same card may appear more than once) and is used as the LazyRow key.
 */
@Parcelize
data class StripItem(
    val cardId: Long,
    val label: String,
    val imageRef: String,
    val isStarter: Boolean,
    val instanceId: String = UUID.randomUUID().toString(),
) : Parcelable {
    companion object {
        fun from(card: CardEntity): StripItem = StripItem(
            cardId = card.id,
            label = card.label,
            imageRef = card.imageRef,
            isStarter = card.type == CardType.SENTENCE_STARTER,
        )
    }
}
