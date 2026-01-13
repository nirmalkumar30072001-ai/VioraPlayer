package com.viora.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viora.player.R
import com.viora.player.adapter.FolderAdapter
import com.viora.player.data.FolderRepository
import com.viora.player.utils.PermissionAware
import com.viora.player.utils.PermissionUtils

class FolderFragment : Fragment(), PermissionAware {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view =
            inflater.inflate(R.layout.fragment_folders, container, false)

        recyclerView = view.findViewById(R.id.recyclerFolders)
        emptyLayout = view.findViewById(R.id.emptyLayout)

        recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 3)

        loadFolders()

        return view
    }

    private fun loadFolders() {

        if (!PermissionUtils.hasVideoPermission(requireActivity())) {
            PermissionUtils.requestVideoPermission(requireActivity())
            return
        }

        val folders =
            FolderRepository.loadFolders(requireContext())

        if (folders.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
            recyclerView.adapter = FolderAdapter(folders)
        }
    }

    // ✅ PermissionAware callback
    override fun onPermissionGranted() {
        loadFolders()
    }

    override fun onResume() {
        super.onResume()
        loadFolders()
    }
}