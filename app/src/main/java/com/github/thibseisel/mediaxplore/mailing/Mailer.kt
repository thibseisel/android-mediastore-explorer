package com.github.thibseisel.mediaxplore.mailing

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.Spanned
import com.github.thibseisel.mediaxplore.R
import com.github.thibseisel.mediaxplore.media.Album
import com.github.thibseisel.mediaxplore.media.Artist
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

class Mailer(private val context: Context) {

    fun sendMediaByEmail(artists: List<Artist>, albums: List<Album>) {
        val subject = context.getString(R.string.mail_subject)
        val message = buildTextContent(artists, albums)

        val sendEmailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT,  message.toHtml())
        }

        val toMailApp = Intent.createChooser(sendEmailIntent, null)
        context.startActivity(toMailApp)
    }

    private fun buildTextContent(artists: List<Artist>, albums: List<Album>): String = buildString {
        appendHTML(prettyPrint = true, xhtmlCompatible = true).html {
            head {
                meta(content = "text/html", charset = "UTF-8")
            }
            body {
                div {
                    h1 { + "Artists" }
                    artistTable(artists)
                    h1 { + "Albums" }
                    albumTable(albums)
                }
            }
        }
    }

    private fun DIV.artistTable(artists: List<Artist>) {
        table {
            thead {
                tr {
                    th { + "ID" }
                    th { + "Name" }
                    th { + "Sort key" }
                    th { + "Songs" }
                    th { + "Albums" }
                }
            }

            tbody {
                for (artist in artists) {
                    tr {
                        td { + artist.id.toString() }
                        td { + artist.name.orEmpty() }
                        td { + artist.sortKey.orEmpty() }
                        td { + artist.trackCount.toString() }
                        td { + artist.albumCount.toString() }
                    }
                }
            }
        }
    }

    private fun DIV.albumTable(albums: List<Album>) {
        table {
            thead {
                tr {
                    th { + "ID" }
                    th { + "Title" }
                    th { + "Sort key" }
                    th { + "Artist" }
                    th { + "Year" }
                    th { + "Album Art" }
                    th { + "Number of songs" }
                }
            }

            tbody {
                for (album in albums) {
                    tr {
                        td { + album.id.toString() }
                        td { + album.title.orEmpty() }
                        td { + album.sortKey.orEmpty() }
                        td { + album.artist.orEmpty() }
                        td { + "${album.firstYear} - ${album.lastYear}"}
                        td { + album.albumArt.orEmpty() }
                        td { + album.numberOfSongs.toString() }
                    }
                }
            }
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

private fun String.toHtml(): Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
} else {
    @Suppress("DEPRECATION")
    Html.fromHtml(this)
}