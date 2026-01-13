package com.viora.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viora.player.R
import com.viora.player.adapter.VideoAdapter
import com.viora.player.model.VideoModel

class VideosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_videos, container, false)

        recyclerView = view.findViewById(R.id.videoRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VideoAdapter(
            onClick = {},
            onMenuAction = { _, _ -> }
        )

        recyclerView.adapter = adapter
        return view
    }

    fun submitVideos(list: List<VideoModel>) {
        if (::adapter.isInitialized) {
            adapter.update(list)
        }
    }
}