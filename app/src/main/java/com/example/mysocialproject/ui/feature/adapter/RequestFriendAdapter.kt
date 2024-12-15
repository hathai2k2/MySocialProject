package com.example.mysocialproject.ui.feature.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemRequestFriendBinding
import com.example.mysocialproject.ui.feature.model.Friendship
import com.github.marlonlom.utilities.timeago.TimeAgo


class RequestFriendAdapter(
    private val context: Context,
    private var friendRequests: MutableList<Friendship>,
    private val onAcceptClick: (Friendship) -> Unit,
    private val onDeclineClick: (Friendship) -> Unit
) : RecyclerView.Adapter<RequestFriendAdapter.MyViewHolder>() {

    class MyViewHolder(val itemRequestFriendBinding: ItemRequestFriendBinding) :
        RecyclerView.ViewHolder(itemRequestFriendBinding.root) {
        fun bind(
            friendship: Friendship,
            onAcceptClick: (Friendship) -> Unit,
            onDeclineClick: (Friendship) -> Unit
        ) {
            Glide.with(itemRequestFriendBinding.root)
                .load(friendship.userAvt)
                .error(R.drawable.avt_base)
                .into(itemRequestFriendBinding.avtRequest)

            val timeAgo = TimeAgo.using(friendship.timeStamp.toDate().time)
            itemRequestFriendBinding.timeStamp.text = timeAgo

            itemRequestFriendBinding.frRequest = friendship
            itemRequestFriendBinding.btnAccept.setOnClickListener { onAcceptClick(friendship) }
            itemRequestFriendBinding.btnUnaccept.setOnClickListener { onDeclineClick(friendship) }
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

    fun updateFriendships(newFriendships: MutableList<Friendship>) {
        if (newFriendships == null) {
            return
        }

        val diffCallback = ReFriendsDiffCallback(friendRequests, newFriendships)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        friendRequests.clear()
        friendRequests.addAll(newFriendships)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeFriendship(friendship: Friendship) {
        val position = friendRequests.indexOf(friendship)
        if (position != -1) {
            friendRequests.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ReFriendsDiffCallback(
        private val oldList: MutableList<Friendship>,
        private val newList: MutableList<Friendship>
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