package com.fatkhun.etemu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.utils.FormatDateTime
import com.fatkhun.core.utils.gone
import com.fatkhun.core.utils.setCustomeTextHTML
import com.fatkhun.core.utils.visible
import com.fatkhun.etemu.databinding.ComponentHistoryBinding

class HistoryPagingAdapter(
    private val context: Context,
    private val callback: Callback
): PagingDataAdapter<LostFoundItemList, HistoryPagingAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<LostFoundItemList>() {
        override fun areItemsTheSame(oldItem: LostFoundItemList, newItem: LostFoundItemList): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(
            oldItem: LostFoundItemList,
            newItem: LostFoundItemList
        ): Boolean {
            return oldItem == newItem
        }
    }

    interface Callback {
        fun onClickItem(pos: Int, item: LostFoundItemList)
        fun onClickDone(pos: Int, item: LostFoundItemList)
    }

    inner class ViewHolder(val binding: ComponentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(
            datas: LostFoundItemList,
            pos: Int,
            callback: Callback
        ) {
            binding.itemTitle.text = setCustomeTextHTML(datas.name)
            binding.itemLocation.text = setCustomeTextHTML(datas.description)
            binding.itemDate.text = FormatDateTime.parse(datas.updatedAt, FormatDateTime.FORMAT_DATE_TIME_YMDTHMSZ,
                FormatDateTime.FORMAT_DATE_TIME_DMYHM_SHORT_MONTH_NO_SEPARATOR)
            binding.itemCategory.text = setCustomeTextHTML(datas.category.name)
            binding.tvStatus.text = if (datas.status.lowercase() == "claimed") "complete".uppercase() else datas.status.uppercase()
            if (datas.status.lowercase() == "open") {
                binding.mbDone.visible()
            } else {
                binding.mbDone.gone()
            }
            binding.mbDetail.setOnClickListener {
                callback.onClickItem(pos, datas)
            }
            binding.mbDone.setOnClickListener {
                callback.onClickDone(pos, datas)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ComponentHistoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bindView(it, position, callback)
        }
    }

    override fun getItemCount(): Int {
        val count = snapshot().size
        return count
    }

}