package com.github.thibseisel.mediaxplore.mailing

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import com.github.thibseisel.mediaxplore.R
import com.github.thibseisel.mediaxplore.media.Album
import com.github.thibseisel.mediaxplore.media.Artist

private const val TYPEFACE_MONOSPACE = "monospace"

private fun String.monospaced(): Spanned = SpannableString(this).apply {
    val textSpan = TypefaceSpan(TYPEFACE_MONOSPACE)
    setSpan(textSpan, 0, this.lastIndex, 0)
}

class Mailer(private val context: Context) {

    fun sendMediaByEmail(artists: List<Artist>, albums: List<Album>) {
        val subject = context.getString(R.string.mail_subject)
        val message = buildTextContent(artists, albums)

        val sendEmailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT,  message)
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

        appendTableInternal(columnHeaders, tableValues)
    }

    private fun Appendable.appendTableInternal(
            columnHeaders: Array<out String>,
            tableValues: Array<Array<String>>
    ) {
        val columnWidths = IntArray(columnHeaders.size) { colIndex ->
            tableValues.fold(columnHeaders[colIndex].length) { acc, valueLine ->
                maxOf(acc, valueLine[colIndex].length)
            }
        }

        for (colIndex in columnHeaders.indices) {
            val header = columnHeaders[colIndex]
            val colWidth = columnWidths[colIndex]
            val emptySpaces = colWidth - header.length

            if (colIndex == 0) {
                this + '|'
            }

            this + (emptySpaces * ' ') + header + '|'
        }

        appendln()

        val totalWidth = columnWidths.sum() + columnHeaders.size + 1
        this + (totalWidth * '-')
        appendln()

        for (lineValues in tableValues) {

            for ((colIndex, value) in lineValues.withIndex()) {
                val colWidth = columnWidths[colIndex]
                val emptySpaces = colWidth - value.length

                if (colIndex == 0) {
                    this + '|'
                }

                this + (emptySpaces * ' ') + value + '|'
            }

            appendln()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance: Mailer? = null

        fun getInstance(context: Context) = instance
                ?: synchronized(this) {
            instance
                    ?: Mailer(context).also { instance = it }
        }
    }
}