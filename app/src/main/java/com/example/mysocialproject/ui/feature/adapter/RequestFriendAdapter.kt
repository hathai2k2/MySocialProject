package com.example.mysocialproject.ui.feature.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemRequestFriendBinding
import com.example.mysocialproject.model.Friend
import com.github.marlonlom.utilities.timeago.TimeAgo


class RequestFriendAdapter(
    private val context: Context,
    private var friendRequests: MutableList<Friend>,
    private val onAcceptClick: (Friend) -> Unit,
    private val onDeclineClick: (Friend) -> Unit
) : RecyclerView.Adapter<RequestFriendAdapter.MyViewHolder>() {

    class MyViewHolder(val itemRequestFriendBinding: ItemRequestFriendBinding) :
        RecyclerView.ViewHolder(itemRequestFriendBinding.root) {
        fun bind(
            friend: Friend,
            onAcceptClick: (Friend) -> Unit,
            onDeclineClick: (Friend) -> Unit
        ) {
            Glide.with(itemRequestFriendBinding.root)
                .load(friend.userAvatar)
                .error(R.drawable.avt_base)
                .into(itemRequestFriendBinding.avtRequest)

            val timeAgo = TimeAgo.using(friend.createdAt.toDate().time)
            itemRequestFriendBinding.timeStamp.text = timeAgo

            itemRequestFriendBinding.frRequest = friend
            itemRequestFriendBinding.btnAccept.setOnClickListener { onAcceptClick(friend) }
            itemRequestFriendBinding.btnUnaccept.setOnClickListener { onDeclineClick(friend) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRequestFriendBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val friendship = friendRequests.get(position)
        holder.bind(friendship, onAcceptClick, onDeclineClick)
    }

    fun updateFriendships(newFriends: MutableList<Friend>) {
        if (newFriends == null) {
            return
        }

        val diffCallback = ReFriendsDiffCallback(friendRequests, newFriends)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        friendRequests.clear()
        friendRequests.addAll(newFriends)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeFriendship(friend: Friend) {
        val position = friendRequests.indexOf(friend)
        if (position != -1) {
            friendRequests.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ReFriendsDiffCallback(
        private val oldList: MutableList<Friend>,
        private val newList: MutableList<Friend>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].userId1 == newList[newItemPosition].userId1
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}