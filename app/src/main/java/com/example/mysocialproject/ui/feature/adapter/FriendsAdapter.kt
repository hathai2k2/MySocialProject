package com.example.mysocialproject.ui.feature.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemFriendBinding
import com.example.mysocialproject.model.User

class FriendsAdapter(
    private val context: Context,
    private var friends: MutableList<User>,
    private val onRemoveFriend: (String, Int) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.MyViewHolder>() {

    class MyViewHolder(private val friendsBinding: ItemFriendBinding) :
        RecyclerView.ViewHolder(friendsBinding.root) {
        fun bind(
            user: User, position: Int,
            onRemoveFriend: (String, Int) -> Unit
        ) {
            Glide.with(friendsBinding.root)
                .load(user.avatarUser)
                .error(R.drawable.avt_base)
                .into(friendsBinding.avtRequest)
            friendsBinding.friends = user
            friendsBinding.btnRemove.setOnClickListener { onRemoveFriend(user.userId, position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = friends.get(position)
        holder.bind(user, position, onRemoveFriend)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    fun updateFriends(newFriends: MutableList<User>?) {
        if (newFriends == null) {
            return
        }

        val diffCallback = FriendsDiffCallback(friends, newFriends)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        friends.clear()
        friends.addAll(newFriends)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeFriend(friend: User) {
        val position = friends.indexOf(friend)
        if (position != -1) {
            friends.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class FriendsDiffCallback(
        private val oldList: MutableList<User>,
        private val newList: MutableList<User>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].userId == newList[newItemPosition].userId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}