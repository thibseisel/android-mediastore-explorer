package com.github.thibseisel.mediaxplore.utils

import android.content.Context
import com.github.thibseisel.mediaxplore.AlbumViewModel
import com.github.thibseisel.mediaxplore.ArtistViewModel
import com.github.thibseisel.mediaxplore.MainActivityViewModel
import com.github.thibseisel.mediaxplore.mailing.Mailer
import com.github.thibseisel.mediaxplore.media.MediaDao

object Injector {

    fun providesMainActivityViewModelFactory(context: Context): MainActivityViewModel.Factory {
        val permissions = PermissionManager.getInstance(context)
        val mediaDao = MediaDao.getInstance(context.contentResolver, permissions)
        val mailer = Mailer.getInstance(context)
        return MainActivityViewModel.Factory(mediaDao, mailer)
    }

    fun providesArtistViewModelFactory(context: Context): ArtistViewModel.Factory {
        val permissions = providesPermissionManager(context)
        val mediaDao = MediaDao.getInstance(context.contentResolver, permissions)
        return ArtistViewModel.Factory(mediaDao)
    }

    fun providesAlbumViewModelFactory(context: Context): AlbumViewModel.Factory {
        val permissions = providesPermissionManager(context)
        val mediaDao = MediaDao.getInstance(context.contentResolver, permissions)
        return AlbumViewModel.Factory(mediaDao)
    }

    private fun providesPermissionManager(context: Context) = PermissionManager.getInstance(context)
}