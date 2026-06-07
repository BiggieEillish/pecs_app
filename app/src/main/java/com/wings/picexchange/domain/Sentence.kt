package com.wings.picexchange.domain

/**
 * Joins the labels currently on the sentence strip into the single line of text that
 * the text-to-speech engine will read aloud (M4). Blank labels are dropped and each
 * label is trimmed so the spoken output stays clean.
 *
 * This is the pure-logic seed of the TTS sequencing and is deliberately framework-free
 * so it can be unit-tested on the JVM (no Android dependencies).
 */
fun spokenSentence(labels: List<String>): String =
    labels.map(String::trim)
        .filter(String::isNotEmpty)
        .joinToString(separator = " ")
