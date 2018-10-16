package com.github.thibseisel.mediaxplore.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.checkSelfPermission

class PermissionManager(private val context: Context) {

    fun requireReadExternalStorage() {
        requirePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun hasExternalStorageRead() = ContextCompat.checkSelfPermission(context,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requirePermission(manifestPermission: String) {
        if (checkSelfPermission(context, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            throw PermissionDeniedException(manifestPermission)
        }
    }

    class PermissionDeniedException(val missingPermission: String) : Exception() {
        override val message get() = "Missing required runtime permission: $missingPermission"
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PermissionManager? = null

        fun getInstance(context: Context) = instance
                ?: synchronized(this) {
            instance
                    ?: PermissionManager(context).also { instance = it }
        }
    }
}