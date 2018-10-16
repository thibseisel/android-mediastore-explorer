package com.github.thibseisel.mediaxplore.media

data class Album(
        val id: Long,
        val title: String?,
        val sortKey: String?,
        val artist: String?,
        val firstYear: Int,
        val lastYear: Int,
        val albumArt: String?,
        val numberOfSongs: Int
)

data class Artist(
        val id: Long,
        val name: String?,
        val sortKey: String?,
        val albumCount: Int,
        val trackCount: Int
)