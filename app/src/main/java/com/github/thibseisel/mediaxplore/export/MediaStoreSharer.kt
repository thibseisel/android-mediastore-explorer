package com.github.thibseisel.mediaxplore.export

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.MediaStore.Audio.Albums
import android.provider.MediaStore.Audio.Artists
import android.support.v4.content.FileProvider
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.util.Log
import com.github.thibseisel.csv.CsvWriter
import com.github.thibseisel.mediaxplore.R
import com.github.thibseisel.mediaxplore.media.Album
import com.github.thibseisel.mediaxplore.media.Artist
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val TYPEFACE_MONOSPACE = "monospace"
private const val EXPORTED_FILENAME = "media.zip"
private const val ARTISTS_FILENAME = "artists.csv"
private const val ALBUMS_FILENAME = "albums.csv"
private const val MIME_TEXT_PLAIN = "text/plain"
private const val PROVIDER_AUTHORITY = "com.github.thibseisel.mediaxplore.fileprovider"

private fun String.monospaced(): Spanned = SpannableString(this).apply {
    val textSpan = TypefaceSpan(TYPEFACE_MONOSPACE)
    setSpan(textSpan, 0, this.lastIndex, 0)
}

class MediaStoreSharer(private val context: Context) {

    /**
     * Share the specified media lists by sending them via an email application as a ZIP file attachment.
     */
    suspend fun shareMediaAsCsv(artists: List<Artist>, albums: List<Album>) {
        val zipFile = withContext(Dispatchers.IO) {
            val shareDir = File(context.cacheDir, "share")
            if (!shareDir.exists()) {
                shareDir.mkdirs()
            }

            File(shareDir, EXPORTED_FILENAME).also {
                writeMediaToZip(it, artists, albums)
            }
        }

        val sharedFileUri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, zipFile)
        val sendEmailIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TEXT_PLAIN
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.mail_subject))
            putExtra(Intent.EXTRA_STREAM, sharedFileUri)
        }

        val toMailApp = Intent.createChooser(sendEmailIntent, null)
        context.startActivity(toMailApp)
    }

    private fun writeMediaToZip(
        zipFile: File,
        artists: List<Artist>,
        albums: List<Album>
    ) {
        try {
            ZipOutputStream(zipFile.outputStream()).use {
                // Write artists detail as a new file in the zip archive
                it.putNextEntry(ZipEntry(ARTISTS_FILENAME))
                writeCsv(
                    it, arrayOf(
                        Artists._ID,
                        Artists.ARTIST,
                        Artists.ARTIST_KEY,
                        Artists.NUMBER_OF_ALBUMS,
                        Artists.NUMBER_OF_TRACKS
                    ), artists
                ) { artist, output ->
                    output[0] = artist.id.toString()
                    output[1] = artist.name
                    output[2] = artist.sortKey
                    output[3] = artist.albumCount.toString()
                    output[4] = artist.trackCount.toString()
                }

                it.closeEntry()

                // Write album details as a new file in the zip archive
                it.putNextEntry(ZipEntry(ALBUMS_FILENAME))
                writeCsv(
                    it, arrayOf(
                        Albums._ID,
                        Albums.ALBUM,
                        Albums.ALBUM_KEY,
                        Albums.ARTIST,
                        Albums.FIRST_YEAR,
                        Albums.LAST_YEAR,
                        Albums.NUMBER_OF_SONGS,
                        Albums.ALBUM_ART
                    ), albums
                ) { album, output ->
                    output[0] = album.id.toString()
                    output[1] = album.artist
                    output[2] = album.sortKey
                    output[3] = album.artist
                    output[4] = album.firstYear.toString()
                    output[5] = album.lastYear.toString()
                    output[6] = album.numberOfSongs.toString()
                    output[7] = album.albumArt
                }

                it.closeEntry()
            }

        } catch (ioe: IOException) {
            Log.e("MediaStoreSharer", "Unexpected error while writing the zip file to be sent.", ioe)
        }
    }

    private fun <T> writeCsv(
        output: OutputStream,
        columns: Array<out CharSequence>,
        values: List<T>,
        transform: (value: T, output: Array<in CharSequence?>) -> Unit
    ) {
        CsvWriter(output.writer()).let { writer ->
            writer.setColumnNames(*columns)

            val buffer = arrayOfNulls<CharSequence>(columns.size)
            for (value in values) {
                transform(value, buffer)

                writer.newRow()
                buffer.forEach(writer::writeField)
            }

            writer.flush()
        }
    }

    /**
     * Share the specified media lists as an ASCII table to another application capable of receiving plain text.
     * User choose which application can receive it and how it is used.
     */
    fun shareMediaTable(artists: List<Artist>, albums: List<Album>) {
        val subject = context.getString(R.string.mail_subject)
        val message = buildTextContent(artists, albums)

        val sendEmailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_HTML_TEXT, message.monospaced())
        }

        val toMailApp = Intent.createChooser(sendEmailIntent, null)
        context.startActivity(toMailApp)
    }

    private fun buildTextContent(artists: List<Artist>, albums: List<Album>): String = buildString {
        appendHeading("Artists")
        appendTable(artists, "ID", "Name", "Tracks", "Albums") { artist, output ->
            output[0] = artist.id.toString()
            output[1] = artist.name.orEmpty()
            output[2] = artist.trackCount.toString()
            output[3] = artist.albumCount.toString()
        }

        appendHeading("Albums")
        appendTable(albums, "ID", "Title", "Artist", "Tracks", "Year", "Album art") { album, output ->
            output[0] = album.id.toString()
            output[1] = album.title.orEmpty()
            output[2] = album.artist.orEmpty()
            output[3] = album.numberOfSongs.toString()
            output[4] = "${album.firstYear} - ${album.lastYear}"
            output[5] = album.albumArt.orEmpty()
        }
    }

    private fun Appendable.appendHeading(title: String) {
        appendln()
        this + (3 * '#') + ' ' + title + ' ' + (3 * '#')
        appendln()
        appendln()
    }

    private inline fun <T> Appendable.appendTable(
        values: List<T>,
        vararg columnHeaders: String,
        columnValueProvider: (value: T, output: Array<String>) -> Unit
    ) {
        val tableValues = Array(values.size) { lineIndex ->
            Array(columnHeaders.size) { "" }.also { output ->
                columnValueProvider(values[lineIndex], output)
            }
        }

        appendTable(columnHeaders, tableValues)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance: MediaStoreSharer? = null

        const val SHARE_FORMAT_TABLE = 1
        const val SHARE_FORMAT_CSV = 2

        fun getInstance(context: Context) = instance
            ?: synchronized(this) {
                instance ?: MediaStoreSharer(context).also { instance = it }
            }
    }
}