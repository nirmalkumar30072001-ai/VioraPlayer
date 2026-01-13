package com.viora.player

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.viora.player.adapter.MainPagerAdapter
import com.viora.player.model.VideoModel

class MainActivity : AppCompatActivity() {

    private val videoList = mutableListOf<VideoModel>()
    private lateinit var pagerAdapter: MainPagerAdapter

    private val PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Viora Player"

        // ViewPager + Tabs
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Video"
                1 -> "Folder"
                else -> "Playlist"
            }
        }.attach()

        checkPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchView =
            menu.findItem(R.id.action_search).actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                pagerAdapter.videosFragment.submitVideos(
                    if (newText.isNullOrBlank()) videoList
                    else videoList.filter {
                        it.name.contains(newText, ignoreCase = true)
                    }
                )
                return true
            }
        })

        return true
    }

    private fun checkPermission() {
        val permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_VIDEO
            else
                Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission)
            == PackageManager.PERMISSION_GRANTED
        ) loadVideos()
        else ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) loadVideos()
    }

    private fun loadVideos() {
        videoList.clear()

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null, null, null,
            MediaStore.Video.Media.DATE_ADDED + " DESC"
        )?.use { cursor ->

            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateCol =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeCol =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )

                videoList.add(
                    VideoModel(
                        uri.toString(),
                        cursor.getString(nameCol),
                        cursor.getLong(durCol),
                        id,
                        cursor.getLong(dateCol),
                        "SD",
                        cursor.getLong(sizeCol),
                        "Local storage"
                    )
                )
            }
        }

        findViewById<ViewPager2>(R.id.viewPager).post {
            pagerAdapter.videosFragment.submitVideos(videoList)
        }
    }
}