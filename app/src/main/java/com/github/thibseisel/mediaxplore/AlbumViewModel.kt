package com.github.thibseisel.mediaxplore

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
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

    init {
        _albums.value = emptyList()
        loadAlbums(null)
    }

    fun changeSorting(sorting: String?) {
        loadAlbums(sorting)
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