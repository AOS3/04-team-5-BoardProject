package com.lion.boardproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lion.boardproject.databinding.ItemReplyListBinding
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.util.setRelativeTimeText

class ReplyAdapter(
    private val listener: OnReplyMenuClickListener,
) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {
    private val items = mutableListOf<ReplyModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        return ReplyViewHolder(
            ItemReplyListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) { view, position -> listener.onMenuClick(view, items[position]) }
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<ReplyModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ReplyViewHolder(
        private val binding: ItemReplyListBinding,
        onMenuClick: (View, Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnReplyMenu.setOnClickListener { view ->
                onMenuClick(view, adapterPosition)
            }
        }

        fun bind(replyModel: ReplyModel) {
            binding.apply {
                tvReplyUserNickName.text = replyModel.replyNickName
                tvReplyContent.text = replyModel.replyText
                tvReplyCreateAt.setRelativeTimeText(replyModel.replyTimeStamp)
            }
        }
    }
}

interface OnReplyMenuClickListener {
    fun onMenuClick(view: View, replyModel: ReplyModel)
}
