package com.fatkhun.etemu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatkhun.core.R
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.utils.FormatDateTime
import com.fatkhun.core.utils.disable
import com.fatkhun.core.utils.enable
import com.fatkhun.core.utils.load
import com.fatkhun.core.utils.setCustomeTextHTML
import com.fatkhun.etemu.databinding.ComponentLostFoundBinding

class LostFoundPagingAdapter(
    private val context: Context,
    private val callback: Callback
): PagingDataAdapter<LostFoundItemList, LostFoundPagingAdapter.ViewHolder>(LostFoundDiffCallback()) {

    class LostFoundDiffCallback : DiffUtil.ItemCallback<LostFoundItemList>() {
        override fun areItemsTheSame(oldItem: LostFoundItemList, newItem: LostFoundItemList): Boolean {
            return oldItem.id == newItem.id
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
    }

    inner class ViewHolder(val binding: ComponentLostFoundBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(
            datas: LostFoundItemList,
            pos: Int,
            callback: Callback
        ) {
            binding.itemImage.load(
                context,
                datas.photo_url
            )
            if (datas.status.lowercase() == "open") {
                if (datas.type.lowercase() == "lost") {
                    binding.clLabel.setBackgroundResource(R.drawable.bg_gradient_lost)
                } else {
                    binding.clLabel.setBackgroundResource(R.drawable.bg_gradient_found)
                }
                binding.tvContentLabel.text = datas.type.uppercase()
                binding.mbDetail.apply {
                    enable()
                    setOnClickListener {
                        callback.onClickItem(pos, datas)
                    }
                }
            } else {
                binding.tvContentLabel.text = "complete".uppercase()
                binding.clLabel.setBackgroundResource(R.drawable.bg_gradient_complete)
                binding.mbDetail.apply {
                    disable()
                    setOnClickListener(null)
                }
            }
            binding.itemTitle.text = setCustomeTextHTML(datas.name)
            binding.itemLocation.text = setCustomeTextHTML(datas.description)
            binding.itemDate.text = FormatDateTime.getInfoTimeUtc(FormatDateTime.FORMAT_DATE_TIME_WITH_TIME_ZONE, datas.updated_at)
            binding.itemCategory.text = setCustomeTextHTML(datas.category_id.name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ComponentLostFoundBinding.inflate(
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