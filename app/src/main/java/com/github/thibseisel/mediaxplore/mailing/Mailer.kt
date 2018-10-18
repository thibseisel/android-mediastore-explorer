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
        heading("Artists")
        artistTable(artists)

        heading("Albums")
        albumTable(albums)
    }

    private fun Appendable.heading(title: String) {
        appendln()
        append(3 * '#').append(' ').append(title).append(' ').append(3 * '#')
        appendln()
        appendln()
    }

    private fun Appendable.artistTable(artists: List<Artist>) {
        val headers = arrayOf("ID", "Name", "Tracks", "Albums")
        val columnValues = arrayOf(
                artists.map { it.id.toString() },
                artists.map { it.name.orEmpty() },
                artists.map { it.trackCount.toString() },
                artists.map { it.albumCount.toString() }
        )

        val columnWidths = IntArray(headers.size) { maxColumnWidth(headers[it], columnValues[it]) }
        for (colIndex in headers.indices) {
            val header = headers[colIndex]
            val colWidth = columnWidths[colIndex]
            val emptySpaces = colWidth - header.length

            if (colIndex == 0) {
                append('|')
            }

            append(emptySpaces * ' ')
            append(header)
            append('|')
        }

        appendln()

        val totalWidth = columnWidths.sum() + headers.size + 1
        append(totalWidth * '-')
        appendln()

        for (lineIndex in artists.indices) {

            for (colIndex in headers.indices) {
                val colWidth = columnWidths[colIndex]
                val colValue = columnValues[colIndex][lineIndex]
                val emptySpaces = colWidth - colValue.length

                if (colIndex == 0) {
                    append('|')
                }

                append(emptySpaces * ' ')
                append(colValue)
                append('|')
            }

            appendln()
        }

        appendln()
    }

    private fun Appendable.albumTable(albums: List<Album>) {
        val headers = arrayOf("ID", "Title", "Tracks", "Year", "Album art")
        val columnValues = arrayOf(
                albums.map { it.id.toString() },
                albums.map { it.title.orEmpty() },
                albums.map { it.numberOfSongs.toString() },
                albums.map { "${it.firstYear} - ${it.lastYear}" },
                albums.map { it.albumArt.orEmpty() }
        )

        val columnWidths = IntArray(headers.size) { maxColumnWidth(headers[it], columnValues[it]) }
        for (colIndex in headers.indices) {
            val header = headers[colIndex]
            val colWidth = columnWidths[colIndex]
            val emptySpaces = colWidth - header.length

            if (colIndex == 0) {
                append('|')
            }

            append(emptySpaces * ' ')
            append(header)
            append('|')
        }

        val totalWidth = columnWidths.sum() + headers.size + 1
        appendln()
        append(totalWidth * '-')

        for (lineIndex in albums.indices) {

            for (colIndex in headers.indices) {
                val colWidth = columnWidths[colIndex]
                val colValue = columnValues[colIndex][lineIndex]
                val emptySpaces = colWidth - colValue.length

                if (colIndex == 0) {
                    append('|')
                }

                append(emptySpaces * ' ')
                append(colValue)
                append('|')
            }

            appendln()
        }

        appendln()
    }

    private fun maxColumnWidth(header: String, values: List<String>): Int {
        return values.fold(header.length) { acc, value -> maxOf(acc, value.length)}
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

private operator fun Int.times(ch: Char): String {
    val length = this.coerceAtLeast(0)
    val array = CharArray(length) { ch }
    return String(array)
}

private fun String.monospaced(): Spanned = SpannableString(this).apply {
    val textSpan = TypefaceSpan(TYPEFACE_MONOSPACE)
    setSpan(textSpan, 0, this.lastIndex, 0)
}