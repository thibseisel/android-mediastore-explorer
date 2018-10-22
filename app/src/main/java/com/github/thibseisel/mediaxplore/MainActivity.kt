package com.github.thibseisel.mediaxplore

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import com.github.thibseisel.mediaxplore.export.MediaStoreSharer
import com.github.thibseisel.mediaxplore.utils.Injector
import kotlinx.android.synthetic.main.activity_main.*


private const val REQUEST_EXTERNAL_STORAGE = 99

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init()
        }
    }

    private fun init() {
        val factory = Injector.providesMainActivityViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory)[MainActivityViewModel::class.java]

        setSupportActionBar(toolbar)
        with(viewPager) {
            adapter = MediaFragmentAdapter(supportFragmentManager, resources)
            tabLayout.setupWithViewPager(this)
        }

        val shareMenu = PopupMenu(this, mailAction)
        shareMenu.inflate(R.menu.menu_share)
        shareMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.share_plain_text -> viewModel.shareMedia(MediaStoreSharer.SHARE_FORMAT_TABLE)
                R.id.share_csv -> viewModel.shareMedia(MediaStoreSharer.SHARE_FORMAT_CSV)
            }

            true
        }

        mailAction.setOnClickListener { shareMenu.show() }
    }
}

private const val TAB_COUNT = 2

class MediaFragmentAdapter(
        fm: FragmentManager,
        private val resources: Resources
) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int = TAB_COUNT

    override fun getItem(position: Int): Fragment? = when (position) {
        0 -> ArtistFragment()
        1 -> AlbumFragment()
        else -> null
    }

    override fun getPageTitle(position: Int): CharSequence? = when(position) {
        0 -> resources.getString(R.string.tab_artists)
        1 -> resources.getString(R.string.tab_albums)
        else -> null
    }
}
