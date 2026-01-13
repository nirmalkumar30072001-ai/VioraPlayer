package com.viora.player.util

import android.net.Uri
import com.viora.player.R

object VideoSourceHelper {

    data class Source(
        val label: String,
        val icon: Int
    )

    fun detect(uriString: String): Source {
        val path = uriString.lowercase()

        return when {
            path.contains("dcim") || path.contains("camera") ->
                Source("Camera", R.drawable.ic_source_camera)

            path.contains("whatsapp") ->
                Source("WhatsApp", R.drawable.ic_source_whatsapp)

            path.contains("snapchat") ->
                Source("Snapchat", R.drawable.ic_source_snapchat)

            path.contains("download") ->
                Source("Download", R.drawable.ic_source_download)

            else ->
                Source("Local", R.drawable.ic_source_folder)
        }
    }
}
