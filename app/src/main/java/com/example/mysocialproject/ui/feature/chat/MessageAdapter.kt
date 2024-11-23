package com.example.mysocialproject.ui.feature.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemMessageReceivedBinding
import com.example.mysocialproject.databinding.ItemMessageSentBinding
import com.example.mysocialproject.model.room.MessageEntity

class MessageAdapter(
    private val messages: List<MessageEntity>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val view = ItemMessageSentBinding.inflate(inflater,parent,false)
            SentMessageViewHolder(view)
        } else {
            val view = ItemMessageReceivedBinding.inflate(inflater,parent,false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(private val itemBinding: ItemMessageSentBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(message: MessageEntity) {
            itemBinding.tvMessageContent.text = message.content
        }
    }

    inner class ReceivedMessageViewHolder(private val itemBinding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(message: MessageEntity) {
            itemBinding.tvMessageContent.text = message.content
        }
    }
}
