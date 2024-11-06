package dev.sogn.radio.shared

private const val MAX_TAG_LENGTH = 23

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= MAX_TAG_LENGTH) tag else tag.substring(0, MAX_TAG_LENGTH)
    }