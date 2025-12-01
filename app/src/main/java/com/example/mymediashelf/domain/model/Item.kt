package com.mymediashelf.app.domain.model

data class Item(
    val id: Long = 0,
    val title: String,
    val type: ItemType,
    val year: Int? = null,
    val status: ItemStatus,
    val favorite: Boolean = false,
    val rating: Float? = null,
    val imdbRating: Float? = null,
    val comment: String? = null,
    val tags: List<Tag> = emptyList()
)

enum class ItemType(val displayName: String) {
    MOVIE("movie"),
    BOOK("book"),
    GAME("game"),
    ANIME("anime")
}

enum class ItemStatus(val displayName: String) {
    PLANNED("planned"),
    WATCHING("watching"),
    WATCHED("watched"),
    READING("reading"),
    PLAYING("playing"),
    COMPLETED("completed")
}

