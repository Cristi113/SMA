package com.mymediashelf.app.domain.model

data class MediaList(
    val id: Long = 0,
    val name: String,
    val items: List<Item> = emptyList()
)