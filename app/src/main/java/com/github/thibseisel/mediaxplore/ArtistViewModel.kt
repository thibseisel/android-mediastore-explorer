package com.github.thibseisel.mediaxplore

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.provider.MediaStore.Audio.Artists
import android.support.annotation.IdRes
import android.util.SparseArray
import com.github.thibseisel.mediaxplore.media.Artist
import com.github.thibseisel.mediaxplore.media.MediaDao
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class ArtistViewModel(
        private val dao: MediaDao
) : ViewModel() {

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = _artists

    private val sortingKeys = SparseArray<String>().apply {
        put(R.id.filter_default, Artists.ARTIST_KEY)
        put(R.id.filter_name, Artists.ARTIST)
        put(R.id.filter_song_count, Artists.NUMBER_OF_TRACKS)
        put(R.id.filter_album_count, Artists.NUMBER_OF_ALBUMS)
    }

    init {
        _artists.value = emptyList()
        loadArtists(null)
    }

    fun handleSortingChange(@IdRes menuItemId: Int): Boolean {
        val sortKey = sortingKeys[menuItemId] ?: return false
        loadArtists(sortKey)
        return true
    }

    private fun loadArtists(sorting: String?) = runBlocking {
        launch(Dispatchers.IO) {
            val artists = dao.getArtists(sorting)
            _artists.postValue(artists)
        }
    }

    class Factory(private val dao: MediaDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = ArtistViewModel(dao) as T
    }
}