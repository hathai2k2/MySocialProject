package com.example.mysocialproject.ui.feature.friend

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemAddFriendBinding
import com.example.mysocialproject.model.FriendshipData
import com.github.marlonlom.utilities.timeago.TimeAgo


class AddFriendAdapter(
    private val context: Context,
    private var friendRequests: MutableList<FriendshipData>,
    private val onAcceptClick: (FriendshipData) -> Unit,
    private val onDeclineClick: (FriendshipData) -> Unit
) : RecyclerView.Adapter<AddFriendAdapter.MyViewHolder>() {

    class MyViewHolder(val itemRequestFriendBinding: ItemAddFriendBinding) :
        RecyclerView.ViewHolder(itemRequestFriendBinding.root) {
        fun bind(
            friendship: FriendshipData,
            onAcceptClick: (FriendshipData) -> Unit,
            onDeclineClick: (FriendshipData) -> Unit
        ) {
            Glide.with(itemRequestFriendBinding.root)
                .load(friendship.userAvt)
                .error(R.drawable.ic_user)
                .into(itemRequestFriendBinding.ivAvatar)

            val timeAgo = TimeAgo.using(friendship.timeStamp.toDate().time)
            itemRequestFriendBinding.timeStamp.text = timeAgo

            itemRequestFriendBinding.tvName.text = friendship.userName
            itemRequestFriendBinding.btnAccept.setOnClickListener { onAcceptClick(friendship) }
            itemRequestFriendBinding.btnUnaccept.setOnClickListener { onDeclineClick(friendship) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemAddFriendBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val friendship = friendRequests.get(position)
        holder.bind(friendship, onAcceptClick, onDeclineClick)
    }

    fun updateFriendships(newFriendships: MutableList<FriendshipData>) {
        if (newFriendships == null) {
            return
        }

        val diffCallback =ReFriendsDiffCallback(friendRequests, newFriendships)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        friendRequests.clear()
        friendRequests.addAll(newFriendships)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeFriendship(friendship: FriendshipData) {
        val position = friendRequests.indexOf(friendship)
        if (position != -1) {
            friendRequests.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ReFriendsDiffCallback(
        private val oldList: MutableList<FriendshipData>,
        private val newList: MutableList<FriendshipData>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].uid1 == newList[newItemPosition].uid1
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}