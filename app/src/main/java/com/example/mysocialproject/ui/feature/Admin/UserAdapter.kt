package com.example.mysocialproject.ui.feature.Admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.databinding.UserItemAdminBinding
import com.example.mysocialproject.model.User

class UserAdapter(
    private var userList: List<User>,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: UserItemAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.userName.text = user.nameUser
            binding.userEmail.text = user.emailUser
            Glide.with(binding.userAvatar.context).load(user.avatarUser).into(binding.userAvatar)

            binding.user = user
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            UserItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    fun updateUsers(newUsers: List<User>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = userList.size
            override fun getNewListSize(): Int = newUsers.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return userList[oldItemPosition].userId == newUsers[newItemPosition].userId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return userList[oldItemPosition] == newUsers[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        userList = newUsers
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }
}
