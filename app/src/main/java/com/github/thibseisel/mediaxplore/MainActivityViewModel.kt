package com.github.thibseisel.mediaxplore

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.github.thibseisel.mediaxplore.koroutines.ScopedViewModel
import com.github.thibseisel.mediaxplore.mailing.Mailer
import com.github.thibseisel.mediaxplore.media.MediaDao
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.coroutineScope

class MainActivityViewModel(
        private val dao: MediaDao,
        private val mailer: Mailer
) : ScopedViewModel() {

    private val sendEmailActor = actor<Unit> {
        consumeEach { _ ->
            coroutineScope {
                val albums = async(context = Dispatchers.IO) { dao.getAlbums(null) }
                val artists = async(context = Dispatchers.IO) { dao.getArtists(null) }
                mailer.sendMediaByEmail(artists.await(), albums.await())
            }
        }
    }

    fun sendMediaByEmail() {
        // Reject incoming email requests while one is pending.
        sendEmailActor.offer(Unit)
    }

    class Factory(
            private val dao: MediaDao,
            private val mailer: Mailer
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = MainActivityViewModel(dao, mailer) as T
    }
}