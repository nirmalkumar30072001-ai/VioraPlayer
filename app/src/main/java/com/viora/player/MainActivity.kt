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
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.viora.player.adapter.MainPagerAdapter
import com.viora.player.model.VideoModel
import com.viora.player.utils.PermissionAware
import com.viora.player.utils.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private val videoList = mutableListOf<VideoModel>()
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Viora Player"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.labelVisibilityMode =
            NavigationBarView.LABEL_VISIBILITY_LABELED
        bottomNav.isItemHorizontalTranslationEnabled = false
        bottomNav.menu.setGroupCheckable(0, true, true)

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

        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                pagerAdapter.videosFragment.submitVideos(
                    if (newText.isNullOrBlank()) videoList
                    else videoList.filter {
                        it.name.contains(newText, true)
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
        ) {
            notifyFragmentsPermissionGranted()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                PermissionUtils.VIDEO_PERMISSION_REQ
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == PermissionUtils.VIDEO_PERMISSION_REQ&&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            notifyFragmentsPermissionGranted()
        }
    }

    private fun notifyFragmentsPermissionGranted() {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is PermissionAware) {
                fragment.onPermissionGranted()
            }
        }
        loadVideosAsync()
    }

    private fun loadVideosAsync() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                loadVideosFromStorage()
            }

            videoList.clear()
            videoList.addAll(list)

            pagerAdapter.videosFragment.submitVideos(videoList)
        }
    }

    private fun loadVideosFromStorage(): List<VideoModel> {
        val list = mutableListOf<VideoModel>()

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Video.Media.DATE_ADDED + " DESC"
        )?.use { cursor ->

            val idCol =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media._ID
                )
            val nameCol =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.DISPLAY_NAME
                )
            val durCol =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.DURATION
                )
            val dateCol =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.DATE_ADDED
                )
            val sizeCol =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Video.Media.SIZE
                )

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                list.add(
                    VideoModel(
                        uri = uri.toString(),
                        name = cursor.getString(nameCol),
                        duration = cursor.getLong(durCol),
                        id = id,
                        dateAdded = cursor.getLong(dateCol),
                        resolution = "SD",
                        size = cursor.getLong(sizeCol),
                        source = "Local storage"
                    )
                )
            }
        }
        return list
    }
}