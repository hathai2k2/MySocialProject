package com.example.mysocialproject.ui.feature.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mysocialproject.databinding.ItemChatBinding
import com.example.mysocialproject.model.room.UserEntity

class ChatAdapter(
    private val listUser:List<UserEntity>,
    private val onCLick:(UserEntity)->Unit
): RecyclerView.Adapter<ChatAdapter.MViewHolder>(){

    inner class MViewHolder(private val itemBinding:ItemChatBinding):RecyclerView.ViewHolder(itemBinding.root){
        fun bind(position: Int){
            val item = listUser[position]
            itemBinding.tvName.text = item.username
            itemBinding.root.setOnClickListener {
                onCLick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        return MViewHolder(ItemChatBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        holder.bind(position)
    }
}