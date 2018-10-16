package com.github.thibseisel.mediaxplore

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
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

    init {
        _artists.value = emptyList()
        loadArtists(null)
    }

    fun changeSorting(sorting: String?) {
        loadArtists(sorting)
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