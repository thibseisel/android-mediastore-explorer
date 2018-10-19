package com.github.thibseisel.mediaxplore.media

import android.content.ContentResolver
import android.provider.MediaStore.Audio.Albums
import android.provider.MediaStore.Audio.Artists
import com.github.thibseisel.mediaxplore.utils.PermissionManager

class MediaDao(
    private val resolver: ContentResolver,
    private val permissions: PermissionManager
) {

    fun getArtists(sorting: String?): List<Artist> {
        require(sorting == null || sorting in ARTIST_COLUMNS) { "Unsupported sorting key: $sorting" }
        if (!permissions.hasExternalStorageRead()) {
            return emptyList()
        }

        return resolver.query(
                Artists.EXTERNAL_CONTENT_URI,
                ARTIST_COLUMNS,
                null,
                null,
                sorting ?: Artists.DEFAULT_SORT_ORDER
        )?.use {
            val colId = it.getColumnIndexOrThrow(Artists._ID)
            val colArtistName = it.getColumnIndexOrThrow(Artists.ARTIST)
            val colArtistKey = it.getColumnIndexOrThrow(Artists.ARTIST_KEY)
            val colAlbumCount = it.getColumnIndexOrThrow(Artists.NUMBER_OF_ALBUMS)
            val colTrackCount = it.getColumnIndexOrThrow(Artists.NUMBER_OF_TRACKS)

            ArrayList<Artist>(it.count).also { artists ->
                while (it.moveToNext()) {
                    artists += Artist(
                            id = it.getLong(colId),
                            name = it.getString(colArtistName),
                            sortKey = it.getString(colArtistKey),
                            albumCount = it.getInt(colAlbumCount),
                            trackCount = it.getInt(colTrackCount)
                    )
                }
            }

        } ?: emptyList()
    }

    fun getAlbums(sorting: String?): List<Album> {
        require(sorting == null || sorting in ALBUM_COLUMNS) { "Unsupported sorting key: $sorting" }
        if (!permissions.hasExternalStorageRead()) {
            return emptyList()
        }

        return resolver.query(
                Albums.EXTERNAL_CONTENT_URI,
                ALBUM_COLUMNS,
                null,
                null,
                sorting ?: Albums.DEFAULT_SORT_ORDER
        )?.use {
            val colId = it.getColumnIndex(Albums._ID)
            val colAlbumName = it.getColumnIndexOrThrow(Albums.ALBUM)
            val colAlbumKey = it.getColumnIndexOrThrow(Albums.ALBUM_KEY)
            val colArtist = it.getColumnIndexOrThrow(Albums.ARTIST)
            val colFirstYear = it.getColumnIndexOrThrow(Albums.FIRST_YEAR)
            val colLastYear = it.getColumnIndexOrThrow(Albums.LAST_YEAR)
            val colAlbumArt = it.getColumnIndexOrThrow(Albums.ALBUM_ART)
            val colSongCount = it.getColumnIndexOrThrow(Albums.NUMBER_OF_SONGS)

            ArrayList<Album>(it.count).also { albums ->
                while (it.moveToNext()) {
                    albums += Album(
                            id = it.getLong(colId),
                            title = it.getString(colAlbumName),
                            sortKey = it.getString(colAlbumKey),
                            artist = it.getString(colArtist),
                            firstYear = it.getInt(colFirstYear),
                            lastYear = it.getInt(colLastYear),
                            albumArt = it.getString(colAlbumArt),
                            numberOfSongs = it.getInt(colSongCount)
                    )
                }
            }

        } ?: emptyList()
    }

    companion object {
        private var instance: MediaDao? = null

        fun getInstance(resolver: ContentResolver, permissions: PermissionManager) =
                instance ?: synchronized(this) {
                    instance ?: MediaDao(resolver, permissions).also { instance = it }
                }
    }
}

private val ARTIST_COLUMNS = arrayOf(
        Artists._ID,
        Artists.ARTIST,
        Artists.ARTIST_KEY,
        Artists.NUMBER_OF_ALBUMS,
        Artists.NUMBER_OF_TRACKS
)

private val ALBUM_COLUMNS = arrayOf(
        Albums._ID,
        Albums.ALBUM,
        Albums.ALBUM_KEY,
        Albums.ARTIST,
        Albums.FIRST_YEAR,
        Albums.LAST_YEAR,
        Albums.ALBUM_ART,
        Albums.NUMBER_OF_SONGS
)