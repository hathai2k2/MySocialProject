package com.example.mysocialproject.ui.feature.chat

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mysocialproject.databinding.FriendlistselcectedlayoutBinding
import com.example.mysocialproject.ui.feature.adapter.FriendSelectedAdapter
import com.example.mysocialproject.ui.feature.viewmodel.MessageViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

@RequiresApi(Build.VERSION_CODES.O)
class FriendListPrivateBottomSheet(
    private val imgUrl: Uri?,
    private val voiceUrl: Uri?,
    private val content: String?
) : BottomSheetDialogFragment() {
    private lateinit var binding: FriendlistselcectedlayoutBinding

    private lateinit var adapter: FriendSelectedAdapter

    private lateinit var messViewModel: MessageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FriendlistselcectedlayoutBinding.inflate(inflater, container, false)
        messViewModel = ViewModelProvider(requireActivity()).get(MessageViewModel::class.java)
        binding.mviewmodel = messViewModel
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.friendListRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = FriendSelectedAdapter(
            requireContext(),
            emptyList(),
            emptyMap(),
            onGetIdfriend = { userId, nameUser, avatarUser, position ->
                openChatWithFriend(userId, nameUser, avatarUser, position)
            },
            updatestate = { userId -> messViewModel.updateMessagesToSeen(userId) },
            viewLifecycleOwner
        )
        binding.friendListRecyclerView.adapter = adapter

        messViewModel.friendsWithMessages.observe(viewLifecycleOwner) { friendsAndMessages ->
            val (friends, lastMessages) = friendsAndMessages
            adapter.updateFriends(friends, lastMessages)
            adapter.notifyDataSetChanged()
        }
        messViewModel.fetchFriendsWithLastMessages()


    }

    private fun openChatWithFriend(
        friendId: String,
        friendName: String,
        friendAvatar: String,
        position: Int
    ) {
        val contentString = content ?: ""
        adapter.updateProgressBarStatus(position, true)

        if (imgUrl != null) {
            messViewModel.uploadAndSendMessage(imgUrl, friendId, contentString, true) // Gửi ảnh
        } else {
            messViewModel.uploadAndSendMessage(
                voiceUrl,
                friendId,
                contentString,
                false
            ) // Gửi voice
        }

        messViewModel.sendSuccess.observe(viewLifecycleOwner) {
            if (it) {
                adapter.updateProgressBarStatus(position, false)
                Snackbar.make(binding.root, "Gửi thành công!", Snackbar.LENGTH_SHORT).show()
                binding.messageEditText.setText("")
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(requireContext(), ItemChatActivity::class.java).apply {
                        putExtra("FRIEND_ID", friendId)
                        putExtra("FRIEND_NAME", friendName)
                        putExtra("FRIEND_AVATAR", friendAvatar)
                    }
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                }, 800)
            } else {
                Snackbar.make(binding.root, "Vui lòng thử lại!", Snackbar.LENGTH_SHORT).show()
            }
        }

    }
}