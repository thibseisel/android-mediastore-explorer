package com.github.thibseisel.mediaxplore

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.github.thibseisel.mediaxplore.koroutines.ScopedViewModel
import com.github.thibseisel.mediaxplore.mailing.MediaStoreSharer
import com.github.thibseisel.mediaxplore.media.MediaDao
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.coroutineScope

class MainActivityViewModel(
        private val dao: MediaDao,
        private val sharer: MediaStoreSharer
) : ScopedViewModel() {

    private val sendEmailActor = actor<Int> {
        consumeEach { format ->
            coroutineScope {
                val albums = async(context = Dispatchers.IO) { dao.getAlbums(null) }
                val artists = async(context = Dispatchers.IO) { dao.getArtists(null) }
                
                if (format == MediaStoreSharer.SHARE_FORMAT_CSV) {
                    sharer.shareMediaAsCsv(artists.await(), albums.await())
                } else {
                    sharer.shareMediaTable(artists.await(), albums.await())
                }
            }
        }
    }

    fun shareMedia(shareFormat: Int) {
        // Reject incoming email requests while one is pending.
        sendEmailActor.offer(shareFormat)
    }

    class Factory(
            private val dao: MediaDao,
            private val sharer: MediaStoreSharer
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = MainActivityViewModel(dao, sharer) as T
    }
}