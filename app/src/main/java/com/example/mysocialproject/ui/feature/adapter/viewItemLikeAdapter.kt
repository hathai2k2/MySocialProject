package com.example.mysocialproject.ui.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemLikeBottomlikedialogBinding

class viewItemLikeAdapter(private val likesList: List<Pair<String, List<String>>>) :
    RecyclerView.Adapter<viewItemLikeAdapter.LikesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val binding = ItemLikeBottomlikedialogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LikesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        val like = likesList[position]
        holder.bind(like)
    }

    override fun getItemCount() = likesList.size

    class LikesViewHolder(private val binding: ItemLikeBottomlikedialogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(like: Pair<String, List<String>>) {
            binding.userNameTextView.text = like.first
            binding.reactionIconsContainer.removeAllViews()
            for (reaction in like.second) {
                val iconView = ImageView(binding.root.context)
                val iconResId = getIconResId(reaction)
                if (iconResId != null) {
                    iconView.setImageResource(iconResId)

                    val layoutParams = LinearLayout.LayoutParams(
                        68,
                        68
                    )
                    layoutParams.marginEnd = 8
                    iconView.layoutParams = layoutParams
                }
                binding.reactionIconsContainer.addView(iconView)
            }
        }

        private fun getIconResId(reaction: String): Int? {
            return when (reaction) {
                "ic_heart" -> R.drawable.ic_heart
                "ic_haha" -> R.drawable.ic_haha
                "ic_sad" -> R.drawable.ic_sad
                "ic_angry" -> R.drawable.ic_angry
                else -> null
            }
        }
    }
}
