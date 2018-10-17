package com.github.thibseisel.mediaxplore

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.provider.MediaStore.Audio.Albums
import android.support.annotation.IdRes
import android.util.SparseArray
import com.github.thibseisel.mediaxplore.koroutines.ScopedViewModel
import com.github.thibseisel.mediaxplore.media.Album
import com.github.thibseisel.mediaxplore.media.MediaDao
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch

class AlbumViewModel(
        private val dao: MediaDao
) : ScopedViewModel() {

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> get() = _albums

    private val sortings = SparseArray<String>().apply {
        put(R.id.filter_default, Albums.DEFAULT_SORT_ORDER)
        put(R.id.filter_title, Albums.ALBUM)
        put(R.id.filter_artist, Albums.ARTIST)
        put(R.id.filter_song_count, Albums.NUMBER_OF_SONGS)
        put(R.id.filter_year, Albums.LAST_YEAR)
    }

    init {
        _albums.value = emptyList()
        loadAlbums(null)
    }

    fun handleSortingChange(@IdRes menuItemId: Int): Boolean {
        val sortKey = sortings[menuItemId] ?: return false
        loadAlbums(sortKey)
        return true
    }

    private fun loadAlbums(sorting: String?) = launch(Dispatchers.IO) {
        val albums = dao.getAlbums(sorting)
        _albums.postValue(albums)
    }

    class Factory(private val mediaDao: MediaDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = AlbumViewModel(mediaDao) as T
    }
}