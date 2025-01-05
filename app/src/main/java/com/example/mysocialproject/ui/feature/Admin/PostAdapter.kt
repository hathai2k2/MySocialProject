package com.example.cameraprovider.Admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mysocialproject.databinding.PostItemAdminBinding
import com.example.mysocialproject.model.Post

class PostAdapter(
    private var postList: List<Post>,
    private val onDeleteClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: PostItemAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, onDeleteClick: (Post) -> Unit) {

            binding.deleteButton.setOnClickListener {
                onDeleteClick(post)
            }
            if (post.photoURL != "" && post.voiceURL == "") {
                binding.linkmedia.text = post.photoURL
            } else {
                binding.linkmedia.text = post.voiceURL
            }
            Glide.with(binding.root.context).load(post.userAvatar).into(binding.imgAvtUserPost)
            binding.post = post
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            PostItemAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.bind(post, onDeleteClick)
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPosts: List<Post>) {

        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = postList.size
            override fun getNewListSize(): Int = newPosts.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return postList[oldItemPosition].postId == newPosts[newItemPosition].postId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return postList[oldItemPosition] == newPosts[newItemPosition]

            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        postList = newPosts
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

}
