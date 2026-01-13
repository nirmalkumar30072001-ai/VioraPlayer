package com.viora.player.adapter

import android.app.AlertDialog
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.viora.player.R
import com.viora.player.model.VideoModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val TYPE_HEADER = 0
private const val TYPE_VIDEO = 1

sealed class ListItem {
    data class Header(val title: String) : ListItem()
    data class Video(val video: VideoModel) : ListItem()
}

class VideoAdapter(
    private val onClick: (VideoModel) -> Unit,
    private val onMenuAction: (VideoModel, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 🔹 ORIGINAL FULL LIST (for filter)
    private val fullList = mutableListOf<VideoModel>()

    // 🔹 DISPLAY LIST (headers + videos)
    private val items = mutableListOf<ListItem>()

    // 🔹 MAIN UPDATE (used initially & after filter)
    fun update(videos: List<VideoModel>) {
        fullList.clear()
        fullList.addAll(videos)
        buildList(videos)
    }

    // 🔹 FILTER FUNCTION
    fun filter(text: String) {
        val filtered = if (text.isBlank()) {
            fullList
        } else {
            fullList.filter {
                it.name.contains(text, ignoreCase = true)
            }
        }
        buildList(filtered)
    }

    // 🔹 GROUPING LOGIC (Month Header)
    private fun buildList(videos: List<VideoModel>) {
        items.clear()

        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        var lastHeader = ""

        videos.forEach { video ->
            val month = sdf.format(Date(video.dateAdded * 1000))
            if (month != lastHeader) {
                items.add(ListItem.Header(month))
                lastHeader = month
            }
            items.add(ListItem.Video(video))
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position] is ListItem.Header) TYPE_HEADER else TYPE_VIDEO

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_HEADER) {
            HeaderVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_month_header, parent, false)
            )
        } else {
            VideoVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video_premium, parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderVH).bind(item.title)
            is ListItem.Video -> {
                (holder as VideoVH).bind(item.video)
                holder.itemView.setOnClickListener { onClick(item.video) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ─────────────────────────────

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.monthTitle)
        fun bind(text: String) {
            title.text = text
        }
    }

    class VideoVH(view: View) : RecyclerView.ViewHolder(view) {

        private val thumb: ImageView = view.findViewById(R.id.videoThumb)
        private val name: TextView = view.findViewById(R.id.videoName)
        private val duration: TextView = view.findViewById(R.id.videoDuration)
        private val meta: TextView = view.findViewById(R.id.videoMeta)

        private val sourceIcon: ImageView = view.findViewById(R.id.sourceIcon)
        private val sourceText: TextView = view.findViewById(R.id.videoSource)
        private val btnMenu: TextView = view.findViewById(R.id.btnMenu)

        fun bind(video: VideoModel) {

            name.text = video.name
            duration.text = format(video.duration)
            meta.text = buildMeta(video)

            sourceText.text = video.source
            sourceIcon.setImageResource(R.drawable.ic_source_folder)

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(itemView.context, Uri.parse(video.uri))
                val frame = retriever.getFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                thumb.setImageBitmap(frame ?: run {
                    thumb.setImageResource(R.drawable.bg_thumb)
                    null
                })
                retriever.release()
            } catch (_: Exception) {
                thumb.setImageResource(R.drawable.bg_thumb)
            }

            btnMenu.setOnClickListener {
                val popup = PopupMenu(
                    it.context,
                    it,
                    Gravity.END,
                    0,
                    R.style.BlackPopupMenu
                )
                popup.menuInflater.inflate(R.menu.menu_video_actions, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.action_delete) {
                        AlertDialog.Builder(
                            itemView.context,
                            R.style.BlackAlertDialog
                        )
                            .setTitle("Delete video?")
                            .setMessage(video.name)
                            .setPositiveButton("Delete") { _, _ ->
                                (itemView.context as? MenuActionHost)
                                    ?.onMenuAction(video, item.itemId)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        (itemView.context as? MenuActionHost)
                            ?.onMenuAction(video, item.itemId)
                    }
                    true
                }
                popup.show()
            }
        }

        private fun buildMeta(video: VideoModel): String {
            val sizeMB = video.size / (1024f * 1024f)
            return "${video.resolution} | ${"%.1f".format(sizeMB)}MB"
        }

        private fun format(ms: Long): String {
            val m = TimeUnit.MILLISECONDS.toMinutes(ms)
            val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
            return String.format("%02d:%02d", m, s)
        }
    }

    interface MenuActionHost {
        fun onMenuAction(video: VideoModel, actionId: Int)
    }
}