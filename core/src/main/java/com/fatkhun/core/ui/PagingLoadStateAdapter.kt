package com.fatkhun.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fatkhun.core.R
import com.fatkhun.core.databinding.ItemNetworkStateBinding

class PagingLoadStateAdapter<T : Any, VH : RecyclerView.ViewHolder>(
    private val adapter: PagingDataAdapter<T, VH>
) : LoadStateAdapter<LoadStateViewHolder>() {

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) =
        holder.bindView(loadState)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = LoadStateViewHolder(
        ItemNetworkStateBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_network_state, parent, false)
        )
    ) {
        adapter.retry()
    }
}