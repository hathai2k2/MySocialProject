package com.example.mysocialproject.ui.feature.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.databinding.ItemFriendselectedBinding
import com.example.mysocialproject.model.Message
import com.example.mysocialproject.model.User

class FriendSelectedAdapter(
    private val context: Context,
    private var friends: List<User>,
    private var lastMessages: Map<String, Message>,
    private val onGetIdfriend: (String, String, String, Int) -> Unit,
    private val updatestate: (String) -> Unit,
    private val viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<FriendSelectedAdapter.MyViewHolder>() {
    private val _progressBarStatus = MutableLiveData<Pair<Int, Boolean>>()
    val progressBarStatus: LiveData<Pair<Int, Boolean>> = _progressBarStatus


    fun updateProgressBarStatus(position: Int, isVisible: Boolean) {
        _progressBarStatus.postValue(Pair(position, isVisible)) // Sử dụng postValue() trong adapter
    }


    inner class MyViewHolder(private val binding: ItemFriendselectedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            user: User,
            onGetIdfriend: (String, String, String, Int) -> Unit,
            updatestate: (String) -> Unit,
            position: Int,
            viewLifecycleOwner: LifecycleOwner
        ) {
            Glide.with(binding.root)
                .load(user.avatarUser)
                .into(binding.avtRequest)
            binding.friend = user


            progressBarStatus.observe(viewLifecycleOwner) { (itemPosition, isVisible) ->
                if (position == itemPosition) {
                    binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
                    binding.avtRequest.visibility = if (isVisible) View.INVISIBLE else View.VISIBLE
                }
            }


            binding.root.setOnClickListener {
                onGetIdfriend(user.userId, user.nameUser, user.avatarUser, position)
                updatestate(user.userId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemFriendselectedBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = friends[position]
        holder.bind(user, onGetIdfriend, updatestate, position, viewLifecycleOwner)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    fun updateFriends(newFriends: List<User>, newLastMessages: Map<String, Message>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = friends.size

            override fun getNewListSize() = newFriends.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return friends[oldItemPosition].userId == newFriends[newItemPosition].userId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldUser = friends[oldItemPosition]
                val newUser = newFriends[newItemPosition]
                val oldMessage = lastMessages[oldUser.userId]
                val newMessage = newLastMessages[newUser.userId]

                return oldUser == newUser && oldMessage?.message == newMessage?.message
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)

        lastMessages = newLastMessages

        friends = newFriends.sortedByDescending { user ->
            newLastMessages[user.userId]?.createdAt?.toLongOrNull() ?: 0L
        }

        diffResult.dispatchUpdatesTo(this)
        Log.d("updateFriends", "Updated lastMessages: $lastMessages")
        Log.d("updateFriends", "Updated friends: $friends")
    }
}