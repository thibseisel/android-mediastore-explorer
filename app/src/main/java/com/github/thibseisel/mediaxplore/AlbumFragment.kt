package com.github.thibseisel.mediaxplore

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.github.thibseisel.mediaxplore.media.Album
import com.github.thibseisel.mediaxplore.utils.Injector
import kotlinx.android.synthetic.main.fragment_albums.*


class AlbumFragment : Fragment() {

    private lateinit var viewModel: AlbumViewModel
    private val adapter = AlbumAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val context = requireContext()
        recyclerView.apply {
            adapter = this@AlbumFragment.adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        val factory = Injector.providesAlbumViewModelFactory(context)
        viewModel = ViewModelProviders.of(this@AlbumFragment, factory)[AlbumViewModel::class.java]

        viewModel.albums.observe(this@AlbumFragment, Observer { albums ->
            if (albums != null) {
                adapter.submitList(albums)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_album_filtering, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.handleSortingChange(item.itemId)) {
            item.isChecked = true
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}

private object AlbumDiffer : DiffUtil.ItemCallback<Album>() {
    override fun areItemsTheSame(a: Album, b: Album): Boolean = a.id == b.id

    override fun areContentsTheSame(a: Album, b: Album): Boolean = a.title == b.title &&
            a.artist == b.artist &&
            a.firstYear == b.firstYear &&
            a.lastYear == b.lastYear &&
            a.numberOfSongs == b.numberOfSongs &&
            a.albumArt == b.albumArt
}

class AlbumAdapter : ListAdapter<Album, AlbumHolder>(AlbumDiffer) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) = AlbumHolder(parent)

    override fun getItemId(position: Int): Long = if (!hasStableIds()) -1L else getItem(position).id

    override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class AlbumHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
) {
    private val title: TextView = itemView.findViewById(R.id.albumTitle)
    private val info: TextView = itemView.findViewById(R.id.albumInfo)
    private val releaseYear: TextView = itemView.findViewById(R.id.albumYear)
    private val albumArtPath: TextView = itemView.findViewById(R.id.albumArtPath)

    fun bind(album: Album) {
        val context = itemView.context
        title.text = album.title
        info.text = context.getString(R.string.albumInfo, album.numberOfSongs)
        releaseYear.text = context.getString(R.string.yearRange, album.firstYear, album.lastYear)
        albumArtPath.text = album.albumArt
    }
}
