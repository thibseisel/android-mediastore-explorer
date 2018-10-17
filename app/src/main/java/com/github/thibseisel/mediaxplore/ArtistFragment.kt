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
import com.github.thibseisel.mediaxplore.media.Artist
import com.github.thibseisel.mediaxplore.utils.Injector
import kotlinx.android.synthetic.main.fragment_artists.*

class ArtistFragment : Fragment() {

    private lateinit var viewModel: ArtistViewModel
    private val adapter = ArtistAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val context = requireActivity()
        recyclerView.apply {
            adapter = this@ArtistFragment.adapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        val factory = Injector.providesArtistViewModelFactory(context)
        viewModel = ViewModelProviders.of(this, factory).get(ArtistViewModel::class.java)

        viewModel.artists.observe(this, Observer { artists ->
            if (artists != null) {
                adapter.submitList(artists)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_artist_filtering, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.handleSortingChange(item.itemId)) {
            item.isChecked = true
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}

private object ArtistDiffer : DiffUtil.ItemCallback<Artist>() {
    override fun areItemsTheSame(a: Artist, b: Artist): Boolean = a.id == b.id

    override fun areContentsTheSame(a: Artist, b: Artist): Boolean = a.name == b.name &&
            a.albumCount == b.albumCount &&
            a.trackCount == b.trackCount
}

class ArtistAdapter : ListAdapter<Artist, ArtistHolder>(ArtistDiffer) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) = ArtistHolder(parent)

    override fun getItemId(position: Int): Long = if (!hasStableIds()) -1L else getItem(position).id

    override fun onBindViewHolder(holder: ArtistHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ArtistHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
) {
    private val name: TextView = itemView.findViewById(R.id.artistName)
    private val info: TextView = itemView.findViewById(R.id.artistInfo)

    fun bind(artist: Artist) {
        val context = itemView.context
        name.text = artist.name
        info.text = context.getString(R.string.artistInfo, artist.trackCount, artist.albumCount)
    }
}