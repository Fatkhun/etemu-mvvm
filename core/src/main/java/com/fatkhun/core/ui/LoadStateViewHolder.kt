package com.fatkhun.core.ui

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.fatkhun.core.databinding.ItemNetworkStateBinding

class LoadStateViewHolder(
    private val binding: ItemNetworkStateBinding,
    private val retryCallback: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener {
            retryCallback()
        }
    }

    fun bindView(loadState: LoadState) {
        with(binding) {
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
            errorMsg.text = (loadState as? LoadState.Error)?.error?.message
        }
    }

}