package com.viora.player.model

/**
 * PLAYit-style video data model
 * Safe • Stable • Extendable
 */
data class VideoModel(

    // 🔥 Actual playable URI (MediaStore content://)
    val uri: String,

    // 🎬 File name
    val name: String,

    // ⏱ Duration in milliseconds
    val duration: Long,

    // 🖼 For REAL thumbnail generation
    val id: Long,

    // 📅 For month grouping (Jan, Dec 2025)
    val dateAdded: Long,

    // 📐 Resolution label (720p / 1080p / 4K)
    val resolution: String = "",

    // 💾 File size in bytes
    val size: Long = 0L,

    // 🏷 Source badge (Camera / WhatsApp / Download)
    val source: String = "Local storage"
)
