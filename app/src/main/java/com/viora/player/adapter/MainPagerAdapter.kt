package com.viora.player.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.viora.player.ui.FoldersFragment
import com.viora.player.ui.PlaylistsFragment
import com.viora.player.ui.VideosFragment

class MainPagerAdapter(activity: FragmentActivity)
    : FragmentStateAdapter(activity) {

    val videosFragment = VideosFragment()
    private val foldersFragment = FoldersFragment()
    private val playlistsFragment = PlaylistsFragment()

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> videosFragment
            1 -> foldersFragment
            else -> playlistsFragment
        }
}