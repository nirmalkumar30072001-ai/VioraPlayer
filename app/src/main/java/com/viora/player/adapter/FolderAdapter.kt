package com.viora.player.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.viora.player.R
import com.viora.player.model.FolderItem

class FolderAdapter(
    private val folderList: List<FolderItem>
) : RecyclerView.Adapter<FolderAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = folderList[position]
        holder.txtFolderName.text = item.name
        holder.txtVideoCount.text = "${item.videoCount} videos"
    }

    override fun getItemCount(): Int = folderList.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val txtFolderName: TextView =
            view.findViewById(R.id.txtFolderName)
        val txtVideoCount: TextView =
            view.findViewById(R.id.txtVideoCount)
    }
}