package com.viora.player.data

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.viora.player.model.FolderItem

object FolderRepository {

    fun loadFolders(context: Context): List<FolderItem> {

        val folderMap = HashMap<String, Int>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Video.Media.RELATIVE_PATH
            else
                MediaStore.Video.Media.DATA
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->

            val pathIndex =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Video.Media.RELATIVE_PATH
                    )
                else
                    cursor.getColumnIndexOrThrow(
                        MediaStore.Video.Media.DATA
                    )

            while (cursor.moveToNext()) {

                val fullPath = cursor.getString(pathIndex)

                val folderName = extractFolderName(fullPath)

                folderMap[folderName] =
                    (folderMap[folderName] ?: 0) + 1
            }
        }

        return folderMap.map {
            FolderItem(it.key, it.value)
        }.sortedByDescending { it.videoCount }
    }

    private fun extractFolderName(path: String): String {
        return path.trimEnd('/')
            .split("/")
            .last { it.isNotBlank() }
    }
}