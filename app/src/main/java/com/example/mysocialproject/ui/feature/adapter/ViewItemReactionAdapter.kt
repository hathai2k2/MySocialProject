package com.example.mysocialproject.ui.feature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ItemLikeBottomlikedialogBinding

class ViewItemReactionAdapter(private val reactionList: List<Pair<String, List<String>>>) :
    RecyclerView.Adapter<ViewItemReactionAdapter.ReactionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionsViewHolder {
        val binding = ItemLikeBottomlikedialogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReactionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReactionsViewHolder, position: Int) {
        val reaction = reactionList[position]
        holder.bind(reaction)
    }

    override fun getItemCount() = reactionList.size

    class ReactionsViewHolder(private val binding: ItemLikeBottomlikedialogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reaction: Pair<String, List<String>>) {
            binding.userNameTextView.text = reaction.first
            binding.reactionIconsContainer.removeAllViews()
            for (emoji in reaction.second) {
                val iconView = ImageView(binding.root.context)
                val iconResId = getIconResId(emoji)
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
